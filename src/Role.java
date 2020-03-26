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
                return PlayNowBot.texturePack.stickerAdventurer();
            case WAECHTERIN:
                return PlayNowBot.texturePack.stickerGuard();
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