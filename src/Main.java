import javax.swing.*;
import java.awt.*;

public final class Main {
    public static void main(String[] args) {
        if(GraphicsEnvironment.isHeadless()){
            System.err.println("A interface requer um ambiente gráfico. Para testar a lógica, execute TestesAlgoritmos.");return;
        }
        SwingUtilities.invokeLater(()->{
            Tema.instalar();TelaPrincipal tela=new TelaPrincipal();tela.setVisible(true);
            if(args.length>0)tela.abrirInicial(java.nio.file.Path.of(args[0]));
        });
    }
}
