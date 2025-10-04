package com.mochaapi.processor;

import com.google.auto.service.AutoService;
import com.mochaapi.annotations.*;
import com.squareup.javapoet.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.io.Writer;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Annotation processor for MochaAPI that generates router registrations and OpenAPI specifications.
 */
@AutoService(Processor.class)
@SupportedAnnotationTypes({
    "com.mochaapi.annotations.RestController",
    "com.mochaapi.annotations.Controller"
})
@SupportedSourceVersion(SourceVersion.RELEASE_21)
public class MochaAPIProcessor extends AbstractProcessor {
    
    private Filer filer;
    private Messager messager;
    private ObjectMapper yamlMapper;
    
    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.filer = processingEnv.getFiler();
        this.messager = processingEnv.getMessager();
        this.yamlMapper = new ObjectMapper(new YAMLFactory());
    }
    
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        try {
            Map<String, RouteInfo> routes = new HashMap<>();
            Map<String, Object> openApiSpec = new HashMap<>();
            
            // Initialize OpenAPI spec
            initializeOpenApiSpec(openApiSpec);
            
            // Process controllers
            for (TypeElement annotation : annotations) {
                for (Element element : roundEnv.getElementsAnnotatedWith(annotation)) {
                    if (element.getKind() == ElementKind.CLASS) {
                        processController((TypeElement) element, routes, openApiSpec);
                    }
                }
            }
            
            // Generate router registrations
            generateRouterRegistrations(routes);
            
            // Generate OpenAPI spec
            generateOpenApiSpec(openApiSpec);
            
        } catch (Exception e) {
            messager.printMessage(Diagnostic.Kind.ERROR, "Error processing annotations: " + e.getMessage());
        }
        
        return true;
    }
    
    private void processController(TypeElement controllerClass, Map<String, RouteInfo> routes, Map<String, Object> openApiSpec) {
        String controllerPath = getControllerPath(controllerClass);
        
        for (Element element : controllerClass.getEnclosedElements()) {
            if (element.getKind() == ElementKind.METHOD) {
                ExecutableElement method = (ExecutableElement) element;
                processMethod(controllerClass, method, controllerPath, routes, openApiSpec);
            }
        }
    }
    
    private void processMethod(TypeElement controllerClass, ExecutableElement method, String controllerPath, 
                             Map<String, RouteInfo> routes, Map<String, Object> openApiSpec) {
        
        // Check for HTTP mapping annotations
        String httpMethod = null;
        String[] paths = {};
        
        if (method.getAnnotation(GetMapping.class) != null) {
            httpMethod = "GET";
            paths = method.getAnnotation(GetMapping.class).value();
        } else if (method.getAnnotation(PostMapping.class) != null) {
            httpMethod = "POST";
            paths = method.getAnnotation(PostMapping.class).value();
        } else if (method.getAnnotation(PutMapping.class) != null) {
            httpMethod = "PUT";
            paths = method.getAnnotation(PutMapping.class).value();
        } else if (method.getAnnotation(DeleteMapping.class) != null) {
            httpMethod = "DELETE";
            paths = method.getAnnotation(DeleteMapping.class).value();
        }
        
        if (httpMethod == null) return;
        
        // Process each path
        for (String path : paths.length > 0 ? paths : new String[]{""}) {
            String fullPath = controllerPath + path;
            String routeKey = httpMethod + " " + fullPath;
            
            RouteInfo routeInfo = new RouteInfo();
            routeInfo.httpMethod = httpMethod;
            routeInfo.path = fullPath;
            routeInfo.controllerClass = controllerClass.getQualifiedName().toString();
            routeInfo.methodName = method.getSimpleName().toString();
            routeInfo.isCpuBound = method.getAnnotation(CpuBound.class) != null;
            
            // Process parameters
            for (VariableElement param : method.getParameters()) {
                ParameterInfo paramInfo = new ParameterInfo();
                paramInfo.name = param.getSimpleName().toString();
                paramInfo.type = param.asType().toString();
                
                if (param.getAnnotation(RequestBody.class) != null) {
                    paramInfo.binding = "body";
                } else if (param.getAnnotation(RequestParam.class) != null) {
                    paramInfo.binding = "query";
                    RequestParam requestParam = param.getAnnotation(RequestParam.class);
                    paramInfo.paramName = requestParam.value().isEmpty() ? paramInfo.name : requestParam.value();
                } else if (param.getAnnotation(RequestHeader.class) != null) {
                    paramInfo.binding = "header";
                    RequestHeader requestHeader = param.getAnnotation(RequestHeader.class);
                    paramInfo.paramName = requestHeader.value().isEmpty() ? paramInfo.name : requestHeader.value();
                } else if (param.getAnnotation(PathVariable.class) != null) {
                    paramInfo.binding = "path";
                    PathVariable pathVariable = param.getAnnotation(PathVariable.class);
                    paramInfo.paramName = pathVariable.value().isEmpty() ? paramInfo.name : pathVariable.value();
                }
                
                routeInfo.parameters.add(paramInfo);
            }
            
            routes.put(routeKey, routeInfo);
            
            // Add to OpenAPI spec
            addToOpenApiSpec(openApiSpec, routeInfo);
        }
    }
    
    private String getControllerPath(TypeElement controllerClass) {
        RestController restController = controllerClass.getAnnotation(RestController.class);
        Controller controller = controllerClass.getAnnotation(Controller.class);
        
        if (restController != null && !restController.value().isEmpty()) {
            return restController.value();
        } else if (controller != null && !controller.value().isEmpty()) {
            return controller.value();
        }
        
        return "";
    }
    
    private void generateRouterRegistrations(Map<String, RouteInfo> routes) throws IOException {
        String className = "RouterRegistration";
        String packageName = "com.mochaapi.generated";
        
        TypeSpec.Builder classBuilder = TypeSpec.classBuilder(className)
            .addModifiers(Modifier.PUBLIC, Modifier.FINAL);
        
        // Add static method to register routes
        MethodSpec.Builder registerMethodBuilder = MethodSpec.methodBuilder("registerRoutes")
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            .returns(void.class)
            .addParameter(ClassName.get("com.mochaapi.runtime", "Router"), "router");
        
        for (RouteInfo route : routes.values()) {
            String routeKey = route.httpMethod + " " + route.path;
            registerMethodBuilder.addStatement(
                "router.addRoute($S, $S, $T.class, $S, $L)",
                route.httpMethod,
                route.path,
                ClassName.bestGuess(route.controllerClass),
                route.methodName,
                route.isCpuBound
            );
        }
        
        classBuilder.addMethod(registerMethodBuilder.build());
        
        JavaFile javaFile = JavaFile.builder(packageName, classBuilder.build())
            .build();
        
        javaFile.writeTo(filer);
    }
    
    private void generateOpenApiSpec(Map<String, Object> openApiSpec) throws IOException {
        // Generate YAML
        try (Writer writer = filer.createResource(StandardLocation.CLASS_OUTPUT, "", "openapi.yaml").openWriter()) {
            yamlMapper.writeValue(writer, openApiSpec);
        }
        
        // Generate JSON
        ObjectMapper jsonMapper = new ObjectMapper();
        try (Writer writer = filer.createResource(StandardLocation.CLASS_OUTPUT, "", "openapi.json").openWriter()) {
            jsonMapper.writeValue(writer, openApiSpec);
        }
    }
    
    private void initializeOpenApiSpec(Map<String, Object> openApiSpec) {
        openApiSpec.put("openapi", "3.0.0");
        openApiSpec.put("info", Map.of(
            "title", "MochaAPI Application",
            "version", "1.0.0",
            "description", "Auto-generated OpenAPI specification for MochaAPI"
        ));
        openApiSpec.put("paths", new HashMap<String, Object>());
    }
    
    private void addToOpenApiSpec(Map<String, Object> openApiSpec, RouteInfo route) {
        @SuppressWarnings("unchecked")
        Map<String, Object> paths = (Map<String, Object>) openApiSpec.get("paths");
        
        String pathKey = route.path.isEmpty() ? "/" : route.path;
        if (!paths.containsKey(pathKey)) {
            paths.put(pathKey, new HashMap<String, Object>());
        }
        
        @SuppressWarnings("unchecked")
        Map<String, Object> pathItem = (Map<String, Object>) paths.get(pathKey);
        
        Map<String, Object> operation = new HashMap<>();
        operation.put("operationId", route.methodName);
        operation.put("summary", route.methodName);
        
        if (!route.parameters.isEmpty()) {
            List<Map<String, Object>> parameters = new ArrayList<>();
            for (ParameterInfo param : route.parameters) {
                Map<String, Object> paramSpec = new HashMap<>();
                paramSpec.put("name", param.paramName != null ? param.paramName : param.name);
                paramSpec.put("in", param.binding);
                paramSpec.put("required", true);
                paramSpec.put("schema", Map.of("type", getOpenApiType(param.type)));
                parameters.add(paramSpec);
            }
            operation.put("parameters", parameters);
        }
        
        pathItem.put(route.httpMethod.toLowerCase(), operation);
    }
    
    private String getOpenApiType(String javaType) {
        if (javaType.equals("java.lang.String")) return "string";
        if (javaType.equals("int") || javaType.equals("java.lang.Integer")) return "integer";
        if (javaType.equals("long") || javaType.equals("java.lang.Long")) return "integer";
        if (javaType.equals("double") || javaType.equals("java.lang.Double")) return "number";
        if (javaType.equals("boolean") || javaType.equals("java.lang.Boolean")) return "boolean";
        return "string";
    }
    
    // Inner classes for data structures
    private static class RouteInfo {
        String httpMethod;
        String path;
        String controllerClass;
        String methodName;
        boolean isCpuBound;
        List<ParameterInfo> parameters = new ArrayList<>();
    }
    
    private static class ParameterInfo {
        String name;
        String type;
        String binding;
        String paramName;
    }
}
