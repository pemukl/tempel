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
    private PlayNowBot bot;
    private SilentSender silent;
    private InlineKeyboardMarkup keyboard;

    public MyMessage(long chatId, SilentSender silent){
        this.silent = silent;
        this.chatId = chatId;
        this.text = "";
    }

    public MyMessage(Message message, PlayNowBot bot){
        this.messageId = message.getMessageId();
        this.bot = bot;
        this.chatId = message.getChatId();
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
        if (silent!=null){
            SendMessage message = new SendMessage();
            message.enableMarkdown(true);
            message.setChatId(chatId);
            message.setText(text);
            message.setReplyMarkup(keyboard);
            silent.execute(message);
        }
        if (bot!=null){
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
