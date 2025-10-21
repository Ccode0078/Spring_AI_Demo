package com.Perrycode.AI_Customer_Support.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.ai.content.Media;
//import javax.print.attribute.standard.Media; //
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class TestController {

    private final ChatClient chatClient;

    // === TEXT CHAT ENDPOINT ====
    @GetMapping
    public String chatWithAI(@RequestParam String msg) {
        List<Message> messages = new ArrayList<>();
        String systemMessage = "You are a helpful AI agent. Respond clearly and concisely to the user's question.";

        messages.add(new SystemMessage(systemMessage));
        messages.add(new UserMessage(msg));

        ChatClient.CallResponseSpec responseSpec = chatClient
                .prompt()
                .messages(messages)
                .options(ChatOptions.builder()
                        .model("gpt-4o")
                        .maxTokens(200)
                        .temperature(0.7)
                        .build())
                .call();

        return responseSpec.content();
    }

    // === IMAGE CHAT ENDPOINT  ==
    @GetMapping(value = "/chat-with-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String chatWithAI(
            @RequestPart("message") String message,
            @RequestPart("image") MultipartFile image
    ) throws IOException {

        // Convert image to a stream resource
        Resource resource = new InputStreamResource(image.getInputStream());
        String systemMessage = "You are a helpful agent. Your goal is to respond to the user question.";

        var responseSpec = chatClient
                .prompt()
                .system(s -> s.text(systemMessage))
                .user(u -> u.text(message)
                        .media(MediaType.parseMediaType(
                                Objects.requireNonNull(image.getContentType())), resource))
                .call();

        return responseSpec.content();
    }

    // === CHAT WITH AUDIO ENDPOINT  ==
    @GetMapping(value = "/chat-with-audio", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String chatWithAudio(
            @RequestPart("message") String message,
            @RequestPart("audio") MultipartFile audio
    ) throws IOException {

        // Convert image to a stream resource
        Resource audioResource = new InputStreamResource(audio.getInputStream());
        Media audioMedia = new Media(MediaType.parseMediaType("audio/mp3"), audioResource);
        String systemPrompt = """
                You are a professional audio translator agent.
                Your tasks are:
                1. Accurately transcribe spoken content from audio files
                2.Provide clear text responses in the same language as the audio
                3.Maintain context between multiple audio inputs""";

        var  responseSpec = chatClient
                .prompt()
                .system(s -> s.text(systemPrompt))
                .user(u -> u.text(message)
                        .media(audioMedia))
                .options(ChatOptions.builder()
                        .model("gpt-4o-audio-preview")
                        .build())
                .call();

        return responseSpec.content();
    }
}
