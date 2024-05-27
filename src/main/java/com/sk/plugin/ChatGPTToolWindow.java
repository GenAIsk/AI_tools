package com.sk.plugin;

import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.IOException;

public class ChatGPTToolWindow {
    private JPanel mainPanel;
    private JTextArea inputTextArea;
    private JButton sendButton;
    private JTextArea outputTextArea;
    private static final String OPENAI_API_KEY = "YOUR API KEY";
    private static final String OPENAI_API_URL = "https://api.openai.com/v1/chat/completions";
    private static final String DEFAULT_INPUT_TEXT = "Enter your prompt here...";

    public ChatGPTToolWindow() {
        // Initialize UI components
        createUIComponents();

        // Set up the send button action
        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String userInput = inputTextArea.getText();
                String response = null;
                try {
                    response = callOpenAI(userInput, "gpt-3.5-turbo", 100, 0.7);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
                outputTextArea.setText(response);
            }
        });
    }

    public JPanel getContent() {
        return mainPanel;
    }

    public static String callOpenAI(String prompt, String model, int maxTokens, double temperature) throws IOException {
        OkHttpClient client = new OkHttpClient();

        // Create the JSON request body
        JSONObject jsonBody = new JSONObject();
        jsonBody.put("model", model);
        JSONArray messages = new JSONArray();
        JSONObject message = new JSONObject();
        message.put("role", "user");
        message.put("content", prompt);
        messages.put(message);
        jsonBody.put("messages", messages);
        jsonBody.put("max_tokens", maxTokens);
        jsonBody.put("temperature", temperature);

        RequestBody body = RequestBody.create(jsonBody.toString(), MediaType.parse("application/json"));

        // Create the HTTP request
        Request request = new Request.Builder()
                .url(OPENAI_API_URL)
                .header("Authorization", "Bearer " + OPENAI_API_KEY)
                .post(body)
                .build();

        // Send the request and get the response
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }
            JSONObject responseBody = new JSONObject(response.body().string());
            return responseBody.getJSONArray("choices").getJSONObject(0).getJSONObject("message").getString("content");
        }
    }

    private void createUIComponents() {
        mainPanel = new JPanel(new BorderLayout());

        // Initialize the text areas and button
        inputTextArea = new JTextArea(DEFAULT_INPUT_TEXT);
        outputTextArea = new JTextArea();
        sendButton = new JButton("Send");

        // Set preferred sizes for the text areas
        inputTextArea.setPreferredSize(new Dimension(400, 100));

        inputTextArea.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (inputTextArea.getText().equals(DEFAULT_INPUT_TEXT)) {
                    inputTextArea.setText("");
                    inputTextArea.setForeground(Color.white);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (inputTextArea.getText().isEmpty()) {
                    inputTextArea.setText(DEFAULT_INPUT_TEXT);
                    inputTextArea.setForeground(Color.white);
                }
            }
        });

        // Set initial color to gray to indicate placeholder text
        inputTextArea.setForeground(Color.white);


        outputTextArea.setPreferredSize(new Dimension(400, 300));

        // Make the output text area non-editable
        outputTextArea.setEditable(false);

        // Add components to the main panel
        mainPanel.add(new JScrollPane(outputTextArea), BorderLayout.CENTER);
        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.add(new JScrollPane(inputTextArea), BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);
        mainPanel.add(inputPanel, BorderLayout.SOUTH);
    }
}
