package src.wsa.gui;

import javafx.geometry.Pos;
import javafx.scene.control.Tab;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

public class Guida {
    public static void creaGuida(Tab tab){
        String guidaString = "          - Per iniziare una nuova esplorazione cliccare sul bottone \"Nuova esplorazione\"; si aprirà una finestra che permetterà\n" +
                "           l'inserimento del dominio e di alcuni seeds iniziali. Nel caso si volesse salvare l'esplorazione in un archivio, spuntare\n" +
                "           la casella interessata e inserire un percorso. Cliccare sul bottone OK per avviare l'esplorazione.\n" +
                "     \n" +
                "         - Per continuare un'esplorazione già archiviata in precedenza, cliccare sul bottone che raffigura una cartella\n" +
                "           e scegliere l'archivio desiderato. L'esplorazione riprenderà automaticamente.\n" +
                "     \n" +
                "         - Mentre è in esecuzione un'esplorazione, è possibile metterla in pausa, terminarla o aggiungere un ulteriore\n" +
                "           seed, tramite rispettivamente i bottoni \"pausa\", \"X\" e \"+\".\n" +
                "           Terminare un'esplorazione chiude la tabella collegata." +
                "     \n" +
                "         - Sempre durante un'esplorazione, è possibile visualizzare alcune informazioni su un sito scaricato\n" +
                "           (Completato/Fallito) cliccando sulla propria riga nella tabella.\n" +
                "     \n" +
                "         - Cliccando sul bottone raffigurante un grafico è possibile visualizzare le statistiche dell'esplorazione\n" +
                "           in corso.\n" +
                "     \n" +
                "         - All'interno delle statistiche del sito sono presenti tre bottoni nella parte bassa della\n" +
                "           finestra. \"Apri sito\" permette l'apertura della pagina all'interno di un browser; \"Mostra links\"\n" +
                "           permette la visualizzazione dei link seguiti, non seguiti o che hanno incontrato degli errori\n" +
                "           all'interno del sito; \"Avanzate\" mostra il numero di immagini e il numero dei nodi dell'albero\n" +
                "           di parsing del sito selezionato.";
        Text guidaTxt = new Text(guidaString);
        guidaTxt.setFont(Font.font(17));
        HBox hb = WindowsManager.createHBox(0,10, Pos.CENTER,guidaTxt, null);

        tab.setContent(hb);
    }
}