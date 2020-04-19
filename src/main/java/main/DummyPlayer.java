package main;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

public class DummyPlayer extends Player {

    public DummyPlayer(Game game) {
        super((game.numberofBots++),("Dummy "+game.numberofBots),game);
    }

    private InlineKeyboardMarkup getKeyboard(List<Player> players) {

        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        for (Player player : players) {
            List<InlineKeyboardButton> rowInline1 = new ArrayList<>();
            String key = "";
            if (currentGame.getActivePlayer() == player)
                key = currentGame.texture.key();
            rowInline1.add(new InlineKeyboardButton().setText(key + player.getName() + key).setCallbackData(player.getId()+";-1"));

            List<InlineKeyboardButton> rowInline2 = new ArrayList<>();
            String[] hiddencards =player.getCards().getHiddenStrings();
            if(player.knowsHisCards) {
                for (int i = 0; i < hiddencards.length; i++) {
                    String str = hiddencards[i];
                    rowInline2.add(new InlineKeyboardButton().setText(str).setCallbackData(player.getId() + ";" + i));
                }
            } else {
                if(currentGame.getActivePlayer() == player)
                    for (int i = 0; i < hiddencards.length; i++) {
                        rowInline2.add(new InlineKeyboardButton().setText("\ud83d\udd0d").setCallbackData(player.getId() + ";" + i));
                    }
                else
                    rowInline2.add(new InlineKeyboardButton().setText("\ud83d\udd0d").setCallbackData(player.getId() + ";" + 0));
            }

            rowsInline.add(rowInline1);
            rowsInline.add(rowInline2);
        }
        markupInline.setKeyboard(rowsInline);
        return markupInline;
    }
}
