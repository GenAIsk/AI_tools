package com.sk.plugin;

import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBTabbedPane;
import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URI;

public class ChatGPTToolWindow {

    private JPanel mainPanel;
    private JTextArea inputTextArea;
    private JTextArea outputTextArea;
    private final JButton sendButton;
    private static final String API = "Enter your API";
    private static final String OPENAI_API_URL = "https://api.openai.com/v1/chat/completions";
    private static final String DEFAULT_INPUT_TEXT = "Enter your prompt here...";
    private JTextArea onlineGPTInputTextArea;

    public ChatGPTToolWindow() {
        sendButton = new JButton("Send");
        createUIComponents();
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
                .header("Authorization", "Bearer " + API)
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
        // Create the main panel with a BorderLayout
        mainPanel = new JPanel(new BorderLayout());
        // Create tabbed pane
        JBTabbedPane tabbedPane = new JBTabbedPane();
        // Create ChatGPT panel
        JPanel chatGPTPanel = new JPanel(new BorderLayout());
        // Initialize the text areas and button
        inputTextArea = new JTextArea(DEFAULT_INPUT_TEXT);
        outputTextArea = new JTextArea();
        JTextField urlTextField = new JTextField("https://www.openai.com");

        // Set preferred sizes for the text areas
        inputTextArea.setPreferredSize(new Dimension(400, 100));
        outputTextArea.setPreferredSize(new Dimension(400, 300));
        urlTextField.setPreferredSize(new Dimension(400, 30));
        // Make the output text area non-editable
        outputTextArea.setEditable(false);
        outputTextArea.setForeground(Color.white);
        // Add focus listener to input text area to clear default text on focus
        inputTextArea.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                if (inputTextArea.getText().equals(DEFAULT_INPUT_TEXT)) {
                    inputTextArea.setText("");
                    inputTextArea.setForeground(Color.white);
                }
            }
            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                if (inputTextArea.getText().isEmpty()) {
                    inputTextArea.setText(DEFAULT_INPUT_TEXT);
                    inputTextArea.setForeground(Color.white);
                }
            }
        });

        // Set initial color to gray to indicate placeholder text
        inputTextArea.setForeground(Color.white);

        // Add components to the ChatGPT panel
        chatGPTPanel.add(new JScrollPane(outputTextArea), BorderLayout.CENTER);
        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.add(new JScrollPane(inputTextArea), BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);
        chatGPTPanel.add(inputPanel, BorderLayout.SOUTH);
        // Add ChatGPT panel to tabbed pane
        tabbedPane.addTab("Online GPT", chatGPTPanel);
        // Create Online GPT panel
        JPanel onlineGPTPanel = new JPanel(new BorderLayout());
        onlineGPTInputTextArea = new JTextArea(DEFAULT_INPUT_TEXT);
        JButton openAIButton = new JButton("Open OpenAI Website");
        openAIButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    Desktop.getDesktop().browse(new URI("https://www.openai.com"));
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        // Add focus listener to online GPT input text area to clear default text on focus
        onlineGPTInputTextArea.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                if (onlineGPTInputTextArea.getText().equals(DEFAULT_INPUT_TEXT)) {
                    onlineGPTInputTextArea.setText("");
                    onlineGPTInputTextArea.setForeground(JBColor.white);
                }
            }
            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                if (onlineGPTInputTextArea.getText().isEmpty()) {
                    onlineGPTInputTextArea.setText(DEFAULT_INPUT_TEXT);
                    onlineGPTInputTextArea.setForeground(JBColor.white);
                }
            }
        });
        onlineGPTPanel.add(openAIButton, BorderLayout.SOUTH);
        onlineGPTPanel.add(urlTextField, BorderLayout.NORTH);
        tabbedPane.addTab("OpenAPI", onlineGPTPanel);
        mainPanel.add(tabbedPane, BorderLayout.CENTER);
    }
}
