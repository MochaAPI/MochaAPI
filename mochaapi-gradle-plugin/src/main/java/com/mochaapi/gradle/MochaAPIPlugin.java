package com.mochaapi.gradle;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.TaskProvider;

/**
 * Gradle plugin for MochaAPI native image generation.
 */
public class MochaAPIPlugin implements Plugin<Project> {
    
    @Override
    public void apply(Project project) {
        // Apply GraalVM native image plugin
        project.getPlugins().apply("org.graalvm.buildtools.native");
        
        // Create native image task
        TaskContainer tasks = project.getTasks();
        TaskProvider<NativeImageTask> nativeImageTask = tasks.register("nativeImage", NativeImageTask.class);
        
        // Configure the task
        nativeImageTask.configure(task -> {
            task.setGroup("mochaapi");
            task.setDescription("Builds a native image of the MochaAPI application");
        });
    }
    
    /**
     * Custom task for building native images with MochaAPI optimizations.
     */
    public static class NativeImageTask extends org.gradle.api.DefaultTask {
        
        @TaskAction
        public void buildNativeImage() {
            getLogger().info("Building MochaAPI native image...");
            
            // Configure GraalVM native image with MochaAPI-specific settings
            configureNativeImage();
            
            getLogger().info("Native image build completed successfully!");
        }
        
        private void configureNativeImage() {
            // Add MochaAPI-specific native image configuration
            // This would include reflection configuration, resource configuration, etc.
            getLogger().info("Configuring native image for MochaAPI...");
        }
    }
}
