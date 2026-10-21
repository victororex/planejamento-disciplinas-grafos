import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

public final class Tema {
    public static final Color FUNDO = new Color(244,247,250);
    public static final Color TINTA = new Color(34,51,73);
    public static final Color SUAVE = new Color(101,117,136);
    public static final Color LINHA = new Color(218,227,235);
    public static final Color VERDE = new Color(17,112,108);
    public static final Color AZUL = new Color(28,63,85);
    public static final Color VERMELHO = new Color(184,58,72);
    private Tema() { }
    public static void instalar() {
        try { UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName()); }
        catch (Exception ignored) { /* Aparência padrão permanece disponível. */ }
        Font fonte = new Font("Segoe UI",Font.PLAIN,13);
        for (Object chave : UIManager.getDefaults().keySet().toArray()) {
            if (chave.toString().endsWith(".font")) UIManager.put(chave,fonte);
        }
        UIManager.put("Panel.background",FUNDO);
        UIManager.put("Label.foreground",TINTA);
        UIManager.put("Button.background",Color.WHITE);
        UIManager.put("Button.foreground",TINTA);
        UIManager.put("Button.focus",LINHA);
        UIManager.put("TabbedPane.selected",Color.WHITE);
        UIManager.put("TabbedPane.contentAreaColor",Color.WHITE);
        UIManager.put("Table.rowHeight",25);
        UIManager.put("ToolTip.background",Color.WHITE);
        UIManager.put("SplitPane.dividerSize",7);
    }
    public static JButton botao(String texto, Runnable acao) {
        JButton b = new JButton(texto);
        b.setFocusPainted(false); b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setMargin(new Insets(7,11,7,11));
        b.addActionListener(e -> acao.run());
        return b;
    }
    public static Border margem(int valor) { return new EmptyBorder(valor,valor,valor,valor); }
}
