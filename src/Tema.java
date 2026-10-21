import javax.swing.*;
import javax.swing.border.*;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.metal.DefaultMetalTheme;
import javax.swing.plaf.metal.MetalLookAndFeel;
import java.awt.*;

/** Paleta compartilhada por Swing, textos HTML e desenho Graphics2D. */
public final class Tema {
    public static final Color FUNDO = new Color(0x111827);
    public static final Color SUPERFICIE = new Color(0x182333);
    public static final Color CAMPO = new Color(0x101b2a);
    public static final Color CONTROLE = new Color(0x26374d);
    public static final Color TINTA = new Color(0xe6edf5);
    public static final Color SUAVE = new Color(0xaab8ca);
    public static final Color LINHA = new Color(0x3b4d65);
    public static final Color VERDE = new Color(0x64dbc4);
    public static final Color AZUL = new Color(0x0e1d2e);
    public static final Color VERMELHO = new Color(0xff9bac);
    public static final Color SELECAO = new Color(0x254e50);
    public static final Color GRADE = new Color(0x26364b);
    public static final Color ARESTA = new Color(0x9aafc6);
    public static final Color CARTAO = new Color(0x203047);
    public static final Color CINZA = new Color(0x58677b);
    public static final Color PRETO = new Color(0x080e18);
    public static final Color FUNDO_PROCESSADO = new Color(0x173f39);
    public static final Color FUNDO_FILA = new Color(0x46391f);
    public static final Color AMARELO = new Color(0xf2cb79);
    public static final Color FUNDO_CICLO = new Color(0x482735);
    public static final Color HALO = new Color(0x2b625e);
    private Tema() { }
    public static void instalar() {
        try {
            // Evita os gradientes claros do tema Ocean sem adicionar dependências.
            MetalLookAndFeel.setCurrentTheme(new MetalEscuro());
            UIManager.setLookAndFeel(new MetalLookAndFeel());
        }
        catch (Exception ignored) { /* Aparência padrão permanece disponível. */ }
        Font fonte = new Font("Segoe UI",Font.PLAIN,13);
        for (Object chave : UIManager.getDefaults().keySet().toArray()) {
            if (chave.toString().endsWith(".font")) UIManager.put(chave,fonte);
        }
        cor("control",SUPERFICIE);cor("info",CONTROLE);cor("text",CAMPO);
        cor("textText",TINTA);cor("textHighlight",SELECAO);cor("textHighlightText",TINTA);
        for(String c : new String[]{"Panel","Viewport","ScrollPane","SplitPane","TabbedPane","ToolBar",
                "OptionPane","FileChooser","List","Table","Tree","TextArea","EditorPane","TextPane"}) cor(c+".background",SUPERFICIE);
        for(String c : new String[]{"Button","ToggleButton","ComboBox","Spinner"}) cor(c+".background",CONTROLE);
        for(String c : new String[]{"TextField","PasswordField","FormattedTextField"}) {
            cor(c+".background",CAMPO);cor(c+".inactiveBackground",SUPERFICIE);
        }
        for(String c : new String[]{"Label","Button","ToggleButton","CheckBox","RadioButton","ComboBox","Spinner",
                "List","Table","TableHeader","Tree","TextArea","TextField","PasswordField","FormattedTextField",
                "EditorPane","TextPane","ToolTip","OptionPane","MenuItem","Menu"}) {
            cor(c+".foreground",TINTA);cor(c+".disabledForeground",SUAVE);cor(c+".disabledText",SUAVE);
        }
        for(String c : new String[]{"TextField","PasswordField","FormattedTextField","TextArea","EditorPane","TextPane"}) {
            cor(c+".caretForeground",VERDE);cor(c+".selectionBackground",SELECAO);
            cor(c+".selectionForeground",TINTA);cor(c+".inactiveForeground",SUAVE);
        }
        for(String c : new String[]{"List","Table","Tree","ComboBox","MenuItem","Menu"}) {
            cor(c+".selectionBackground",SELECAO);cor(c+".selectionForeground",TINTA);
        }
        cor("CheckBox.background",SUPERFICIE);cor("RadioButton.background",SUPERFICIE);
        cor("Button.select",SELECAO);cor("Button.focus",VERDE);cor("ToggleButton.select",SELECAO);
        cor("ToggleButton.focus",VERDE);cor("CheckBox.focus",VERDE);
        cor("OptionPane.messageForeground",TINTA);cor("ComboBox.disabledBackground",FUNDO);
        cor("TabbedPane.foreground",SUAVE);cor("TabbedPane.selected",CONTROLE);
        cor("TabbedPane.selectedForeground",TINTA);cor("TabbedPane.contentAreaColor",SUPERFICIE);
        cor("TabbedPane.focus",VERDE);cor("TabbedPane.highlight",LINHA);cor("TabbedPane.shadow",LINHA);
        cor("TableHeader.background",CONTROLE);cor("Table.gridColor",LINHA);
        cor("Table.focusCellBackground",SELECAO);cor("Table.focusCellForeground",TINTA);
        cor("ToolTip.background",CONTROLE);cor("MenuItem.background",SUPERFICIE);cor("PopupMenu.background",SUPERFICIE);
        cor("ScrollBar.background",FUNDO);cor("ScrollBar.track",FUNDO);cor("ScrollBar.thumb",LINHA);
        cor("ScrollBar.thumbHighlight",SUAVE);cor("ScrollBar.thumbShadow",FUNDO);cor("ScrollBar.thumbDarkShadow",FUNDO);
        UIManager.put("ScrollBar.width",14);
        UIManager.put("Table.rowHeight",25);
        UIManager.put("SplitPane.dividerSize",7);
        UIManager.put("ToolTip.border",new LineBorder(LINHA));
    }
    private static void cor(String chave,Color valor) { UIManager.put(chave,new ColorUIResource(valor)); }
    public static String hex(Color cor) { return String.format("#%02x%02x%02x",cor.getRed(),cor.getGreen(),cor.getBlue()); }
    public static JButton botao(String texto, Runnable acao) {
        JButton b = new JButton(texto);
        b.setFocusPainted(false); b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setMargin(new Insets(7,11,7,11));
        b.addActionListener(e -> acao.run());
        return b;
    }
    public static Border margem(int valor) { return new EmptyBorder(valor,valor,valor,valor); }

    private static final class MetalEscuro extends DefaultMetalTheme {
        @Override public String getName() { return "Planejamento Escuro"; }
        @Override protected ColorUIResource getPrimary1() { return new ColorUIResource(LINHA); }
        @Override protected ColorUIResource getPrimary2() { return new ColorUIResource(VERDE); }
        @Override protected ColorUIResource getPrimary3() { return new ColorUIResource(SELECAO); }
        @Override protected ColorUIResource getSecondary1() { return new ColorUIResource(LINHA); }
        @Override protected ColorUIResource getSecondary2() { return new ColorUIResource(CONTROLE); }
        @Override protected ColorUIResource getSecondary3() { return new ColorUIResource(SUPERFICIE); }
        @Override protected ColorUIResource getBlack() { return new ColorUIResource(TINTA); }
        @Override protected ColorUIResource getWhite() { return new ColorUIResource(SUPERFICIE); }
    }
}
