import org.telegram.abilitybots.api.sender.SilentSender;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class MyMessage {
    private String text;
    private long chatId;
    private int messageId;
    private PlayNowBot bot;
    private SilentSender silent;
    private InlineKeyboardMarkup keyboard;

    public MyMessage(long chatId, SilentSender silent){
        System.out.println("New MyMessage.");
        this.silent = silent;
        this.chatId = chatId;
        this.text = "";
    }

    public MyMessage(long chatId, int messageId, PlayNowBot bot){
        this.messageId = messageId;
        this.bot = bot;
        this.chatId = chatId;
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
                System.out.println("Error sending: " + e.getMessage());
            }
        }
    }
}
