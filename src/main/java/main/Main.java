package main;

import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.LongPollingBot;

import java.io.*;


public class Main {

    public static void main(String[] args) {
        String telegramBotToken = null, telegramBotName = null;
        long telegramAdminChatID = -1;
        File file = new File("config.dat");
        boolean fileCreated = true;
        try {
            fileCreated = file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (!fileCreated) {
            FileReader fileReader = null;
            try {
                fileReader = new FileReader(new File("config.dat"));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            if (fileReader != null) {
                BufferedReader bufferedReader = new BufferedReader(fileReader);
                String line;
                try {
                    while ((line = bufferedReader.readLine()) != null) {
                        String[] split = line.split("=");
                        if (split.length < 2)
                            continue;
                        if (split[0].equalsIgnoreCase("telegramBotToken"))
                            telegramBotToken = split[1];
                        else if (split[0].equalsIgnoreCase("telegramAdminChatID"))
                            telegramAdminChatID = Long.parseLong(split[1]);
                        else if (split[0].equalsIgnoreCase("telegramBotName"))
                            telegramBotName = split[1];
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        if (telegramAdminChatID == -1 || telegramBotToken == null || telegramBotName == null || fileCreated) {
            try {
                FileWriter fileWriter = new FileWriter(file);
                fileWriter.append("telegramBotToken=").append(telegramBotToken == null ? "" : telegramBotToken).append('\n');
                fileWriter.append("telegramAdminChatID=").append(String.valueOf(telegramAdminChatID == -1 ? "" : telegramAdminChatID)).append('\n');
                fileWriter.append("telegramBotName=").append(telegramBotName == null ? "" : telegramBotName).append('\n');
                fileWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (!fileCreated)
                System.out.println("[Error] Secret configuration could not be loaded correctly! Cleaned config file! Please fill the needed information.");
            else
                System.out.println("[Info] New config file was created. Please fill the needed information.");
            return;
        }

        ApiContextInitializer.init();
        TelegramBotsApi botsApi = new TelegramBotsApi();
        try {
            LongPollingBot playNowBot = new PlayNowBot(telegramBotToken, telegramBotName, telegramAdminChatID);
            botsApi.registerBot(playNowBot);
            MyMessage.setBot((PlayNowBot) playNowBot);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}