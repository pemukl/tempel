import org.telegram.telegrambots.meta.api.methods.send.SendSticker;
import org.telegram.telegrambots.meta.api.objects.stickers.Sticker;

public enum Role {
    ABENTEURER,
    WAECHTERIN;

    public String getEmoji(){
        switch (this){
            case ABENTEURER:
                return PlayNowBot.texturePack.adventurer();
            case WAECHTERIN:
                return PlayNowBot.texturePack.guard();
        }
        return "no emoji found";
    }

    public String getStickerID(){
        switch (this){
            case ABENTEURER:
                return "CAACAgIAAxkBAAIODV57YBlg3HdFI7BDXf1vkXM8n5fqAAIgAAOWn4wOrP1BM_Sqb_kYBA";
            case WAECHTERIN:
                return "CAACAgIAAxkBAAINUV57S2xDYExq74na24Sy4u4FeYi_AAKmAAP3AsgPqwzk86kqxlgYBA";
        }
        return null;
    }

    @Override
    public String toString() {
        if (this == ABENTEURER)
            return "Abenteurer";
        else
            return "Wächterin";
    }
}