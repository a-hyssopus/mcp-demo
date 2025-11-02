package org.travel.travelapp.config;

import io.micrometer.observation.ObservationRegistry;
import io.modelcontextprotocol.client.McpSyncClient;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.observation.ChatClientObservationConvention;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.model.chat.client.autoconfigure.ChatClientBuilderConfigurer;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.tools.Tool;
import java.util.List;

@Configuration
public class ChatClientConfig {

    /**
     * Local LLM (Gemma3 via Docker Model Runner)
     * Used for sanitization and lightweight tasks
     */
    @Bean
    public ChatClient localGemmaChatClient(ChatClientBuilderConfigurer configurer,
                                           ChatModel openAiChatModel,
                                           ObjectProvider<ObservationRegistry> observationRegistryProvider,
                                           ObjectProvider<ChatClientObservationConvention> observationConventionProvider) {

        ChatClient.Builder builder = ChatClient.builder(
                openAiChatModel,
                observationRegistryProvider.getIfUnique(() -> ObservationRegistry.NOOP),
                observationConventionProvider.getIfUnique(() -> null)
        );

        return configurer.configure(builder).build();
    }

    /**
     * Anthropic Claude Chat Client
     * Used for trip planning and MCP integrations
     */
    @Bean
    public ChatClient claudeChatClient(ChatClientBuilderConfigurer configurer,
                                       ChatModel anthropicChatModel,
                                       ObjectProvider<ObservationRegistry> obsRegistry,
                                       ObjectProvider<ChatClientObservationConvention> obsConvention,
                                       List<McpSyncClient> mcpSyncClients,
                                       ObjectProvider<List<Tool>> extraToolsProvider) {

        ChatClient.Builder builder = ChatClient.builder(
                anthropicChatModel,
                obsRegistry.getIfUnique(() -> ObservationRegistry.NOOP),
                obsConvention.getIfUnique(() -> null)
        );

        ToolCallbackProvider provider = new SyncMcpToolCallbackProvider(mcpSyncClients);

        // Get tools from all clients
        ToolCallback[] tools = provider.getToolCallbacks();

        builder.defaultToolCallbacks(tools);


        return configurer.configure(builder).build();
    }
}
