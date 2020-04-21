package main;

import org.telegram.abilitybots.api.sender.SilentSender;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class MyMessage {
    private String text;
    private long chatId;
    private int messageId;
    private static PlayNowBot bot;
    private SilentSender silent;
    private InlineKeyboardMarkup keyboard;

    public static void setBot(PlayNowBot botP){
        bot = botP;
    }

    public MyMessage(long chatId, SilentSender silent){
        this.silent = silent;
        this.chatId = chatId;
        this.text = "";
    }

    public MyMessage(Message message){
        this.messageId = message.getMessageId();
        this.chatId = message.getChatId();
        this.text = "";
    }

    public MyMessage(){
        this.messageId = -1;
        this.chatId = bot.creatorId();
        this.text = "";
    }


    public void append(String text){
        this.text = this.text + text;
    }

    public void setReplyMarkup(InlineKeyboardMarkup keyboard){
        this.keyboard = keyboard;
    }

    public void setText(String text){
        this.text = text;
    }

    public void setChatId(long chatId){
        this.chatId = chatId;
    }

    public void send() {
        if (silent!=null || chatId == bot.creatorId()){
            SendMessage message = new SendMessage();
            message.enableMarkdown(true);
            message.setChatId(chatId);
            message.setText(text);
            message.setReplyMarkup(keyboard);
            silent.execute(message);
        }
        if (chatId!=-1){
            EditMessageText message = new EditMessageText();
            message.enableMarkdown(true);
            message.setChatId(chatId);
            message.setText(text);
            message.setReplyMarkup(keyboard);
            message.setMessageId(messageId);
            try {
                bot.execute(message);
            } catch (TelegramApiException e) {
                if(e.toString().contains("[400]"))
                    return;

                if(e.toString().contains("[429]")) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                    this.send();
                    return;
                }

                System.out.println("Error sending: \""+ message.getText() + "\" \r\n"
                        + e.toString());

            }
        }
    }


}
