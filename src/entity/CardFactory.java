package entity;

import java.sql.ResultSet;
import java.sql.SQLException;

public class CardFactory {

    public static Card createCardFromResultSet(ResultSet rs) throws SQLException {
        int cardId = rs.getInt("card_id");
        String cardName = rs.getString("card_name");
        String manaCost = rs.getString("card_mana_cost");
        String cardType = rs.getString("card_type");
        String cardSubtype = rs.getString("card_subtype");
        String powerStr = rs.getString("card_power");
        String toughnessStr = rs.getString("card_toughness");
        String cardText = rs.getString("card_text");
        String cardEdition = rs.getString("card_edition");
        String cardStatus = rs.getString("card_status");

        // Convert power and toughness to integers
        Integer power = null;
        Integer toughness = null;

        try {
            if (powerStr != null && !powerStr.isEmpty() && !powerStr.equals("*")) {
                power = Integer.parseInt(powerStr);
            }
            if (toughnessStr != null && !toughnessStr.isEmpty() && !toughnessStr.equals("*")) {
                toughness = Integer.parseInt(toughnessStr);
            }
        } catch (NumberFormatException e) {
            // If it's not a number leave as null
            System.out.println("Warning: Could not parse power/toughness for card: " + cardName);
        }

        String typeLower = cardType.toLowerCase();

        if (typeLower.contains("creature")) {
            return new Creature(
                    cardId, cardName, manaCost != null ? manaCost : "",
                    cardType, cardSubtype != null ? cardSubtype : "",
                    power != null ? power : 0,
                    toughness != null ? toughness : 0,
                    cardText != null ? cardText : "",
                    cardEdition != null ? cardEdition : "",
                    cardStatus != null ? cardStatus : "Legal"
            );
        } else if (typeLower.contains("artifact")) {
            return new Artifact(
                    cardId, cardName, manaCost != null ? manaCost : "",
                    cardType, cardSubtype != null ? cardSubtype : "",
                    cardText != null ? cardText : "",
                    cardEdition != null ? cardEdition : "",
                    cardStatus != null ? cardStatus : "Legal"
            );
        } else if (typeLower.contains("instant")) {
            return new Instant(
                    cardId, cardName, manaCost != null ? manaCost : "",
                    cardType, cardSubtype != null ? cardSubtype : "",
                    cardText != null ? cardText : "",
                    cardEdition != null ? cardEdition : "",
                    cardStatus != null ? cardStatus : "Legal"
            );
        } else if (typeLower.contains("sorcery")) {
            return new Sorcery(
                    cardId, cardName, manaCost != null ? manaCost : "",
                    cardType, cardSubtype != null ? cardSubtype : "",
                    cardText != null ? cardText : "",
                    cardEdition != null ? cardEdition : "",
                    cardStatus != null ? cardStatus : "Legal"
            );
        } else if (typeLower.contains("land")) {
            return new Land(
                    cardId, cardName, cardType, cardSubtype != null ? cardSubtype : "",
                    cardText != null ? cardText : "",
                    cardEdition != null ? cardEdition : "",
                    cardStatus != null ? cardStatus : "Legal"
            );
        } else if (typeLower.contains("enchantment")) {
            return new Artifact(
                    cardId, cardName, manaCost != null ? manaCost : "",
                    cardType, cardSubtype != null ? cardSubtype : "",
                    cardText != null ? cardText : "",
                    cardEdition != null ? cardEdition : "",
                    cardStatus != null ? cardStatus : "Legal"
            );
        } else {
            System.out.println("Unknown card type: " + cardType + " for card: " + cardName + ". Defaulting to Artifact.");
            return new Artifact(
                    cardId, cardName, manaCost != null ? manaCost : "",
                    cardType, cardSubtype != null ? cardSubtype : "",
                    cardText != null ? cardText : "",
                    cardEdition != null ? cardEdition : "",
                    cardStatus != null ? cardStatus : "Legal"
            );
        }
    }

    public static Card createCard(int cardId, String cardName, String manaCost, String cardType,
                                  String cardSubtype, String power, String toughness,
                                  String cardText, String cardEdition, String cardStatus) {

        if (power != null && !power.isEmpty() && toughness != null && !toughness.isEmpty()) {
            try {
                int p = Integer.parseInt(power);
                int t = Integer.parseInt(toughness);
                return new Creature(cardId, cardName, manaCost, cardType, cardSubtype,
                        p, t, cardText, cardEdition, cardStatus);
            } catch (NumberFormatException e) {
            }
        }
        return new Artifact(cardId, cardName, manaCost, cardType, cardSubtype,
                cardText, cardEdition, cardStatus);
    }
}