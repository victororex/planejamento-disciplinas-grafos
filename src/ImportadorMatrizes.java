import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.io.*;
import java.util.*;

/** Importação transacional: prévia independente do projeto aberto. Nada acessa a rede. */
public final class ImportadorMatrizes {
    public record Previa(ProjetoAcademico projeto, List<String> erros, List<String> avisos) {
        public Previa { erros=List.copyOf(erros);avisos=List.copyOf(avisos); }
        public boolean valida() { return projeto!=null && erros.isEmpty(); }
        public String resumo() {
            int ds=projeto==null?0:projeto.grafo().quantidadeVertices();
            int rs=projeto==null?0:projeto.grafo().quantidadeArestas()+projeto.grafo().getCodigos().stream().mapToInt(c->projeto.grafo().requisitos(c).size()).sum();
            return (projeto==null?"Importação rejeitada; contagens indisponíveis.":ds+" disciplinas; "+rs+" relações/regras de topo.\n"+projeto.matriz().identificacao())
                +"\nErros: "+(erros.isEmpty()?"nenhum":String.join("\n",erros))+"\nAvisos: "+(avisos.isEmpty()?"nenhum":String.join("\n",avisos));
        }
    }
    public Previa prever(Path caminho) {
        List<String> erros=new ArrayList<>(), avisos=new ArrayList<>();ProjetoAcademico p=null;
        try {
            if(Files.size(caminho)>8*1024*1024)throw new IOException("Arquivo maior que 8 MiB.");
            String ext=caminho.toString().toLowerCase(Locale.ROOT);
            if(ext.endsWith(".pgd"))p=new GerenciadorArquivos().abrir(caminho);
            else {
                String texto=Files.readString(caminho,StandardCharsets.UTF_8);if(texto.startsWith("\ufeff"))texto=texto.substring(1);
                if(ext.endsWith(".csv"))p=csv(texto,erros,avisos);
                else if(ext.endsWith(".json"))p=new CatalogoJson(avisos).ler(Json.ler(texto));
                else throw new IllegalArgumentException("Selecione PGD, CSV ou JSON.");
            }
            if(p!=null && new DetectorCiclos().analisar(p.grafo()).possuiCiclo())avisos.add("Há ciclo de pré-requisitos: ordenação e planejamento por níveis ficam bloqueados.");
        } catch(IOException | IllegalArgumentException | NullPointerException e) { erros.add(e.getMessage()==null?"Campo obrigatório ausente.":e.getMessage()); }
        return new Previa(erros.isEmpty()?p:null,erros,avisos);
    }
    private ProjetoAcademico csv(String texto,List<String> erros,List<String> avisos) {
        String primeira=texto.lines().findFirst().orElse("");char separador=primeira.contains(";")?';':',';
        List<List<String>> linhas=lerCsv(texto,separador);if(linhas.isEmpty())throw new IllegalArgumentException("CSV vazio.");
        List<String> cab=linhas.get(0);Set<String> conhecidos=Set.of("codigo","nome","periodo","horas","creditos","categoria","status","descricao","observacoes","oferta");
        if(new HashSet<>(cab).size()!=cab.size())throw new IllegalArgumentException("Cabeçalho CSV duplicado.");
        for(String k:cab)if(!conhecidos.contains(k))avisos.add("Campo não reconhecido: "+k);
        if(!cab.contains("codigo")||!cab.contains("nome"))throw new IllegalArgumentException("CSV exige codigo e nome.");
        GrafoDisciplinas g=new GrafoDisciplinas();Map<String,VerticeVisual> ps=new LinkedHashMap<>();
        if(linhas.size()>2001)throw new IllegalArgumentException("Limite de 2000 disciplinas.");
        for(int i=1;i<linhas.size();i++)try {
            List<String> linha=linhas.get(i);if(linha.size()!=cab.size())throw new IllegalArgumentException("Quantidade de colunas incorreta.");
            Map<String,String> m=new HashMap<>();for(int j=0;j<cab.size();j++)m.put(cab.get(j),linha.get(j));
            String status=m.getOrDefault("status","");String per=m.getOrDefault("periodo","");
            Disciplina d=new Disciplina(m.get("codigo"),m.get("nome"),per.isBlank()?0:Integer.parseInt(per),decimal(m.get("horas")),decimal(m.get("creditos")),m.get("categoria"),m.get("descricao"),m.get("observacoes"),m.get("oferta"),status.isBlank()?StatusAcademico.NAO_CURSADA:StatusAcademico.valueOf(status));
            g.adicionarDisciplina(d);int n=ps.size();ps.put(d.getCodigo(),new VerticeVisual(140+n%20*240,110+n/20*95));
        } catch(IllegalArgumentException e){erros.add("Registro CSV "+(i+1)+": "+e.getMessage());}
        return new ProjetoAcademico(g,ps);
    }
    private Double decimal(String s){return s==null||s.isBlank()?null:Double.valueOf(s.replace(',','.'));}
    static List<List<String>> lerCsv(String texto,char sep) {
        List<List<String>> linhas=new ArrayList<>();List<String> linha=new ArrayList<>();StringBuilder campo=new StringBuilder();boolean aspas=false,fechado=false;
        for(int i=0;i<texto.length();i++) {
            char c=texto.charAt(i);
            if(aspas){if(c=='"'){if(i+1<texto.length()&&texto.charAt(i+1)=='"'){campo.append('"');i++;}else{aspas=false;fechado=true;}}else campo.append(c);}
            else if(c==sep||c=='\n'||c=='\r'){
                linha.add(campo.toString());campo.setLength(0);fechado=false;
                if(c!=sep){if(c=='\r'&&i+1<texto.length()&&texto.charAt(i+1)=='\n')i++;linhas.add(List.copyOf(linha));linha.clear();}
            }else if(c=='"' && campo.length()==0 && !fechado)aspas=true;
            else {if(fechado||c=='"')throw new IllegalArgumentException("Aspas CSV inválidas.");campo.append(c);}
        }
        if(aspas)throw new IllegalArgumentException("Campo CSV sem fechamento.");
        if(campo.length()>0||!linha.isEmpty()||fechado){linha.add(campo.toString());linhas.add(List.copyOf(linha));}
        return linhas;
    }
    public void exportarJson(Path path,ProjetoAcademico p) throws IOException { gravar(path,Json.escrever(CatalogoJson.escrever(p))+"\n"); }
    public void exportarCsv(Path path,ProjetoAcademico p) throws IOException {
        StringBuilder s=new StringBuilder("codigo,nome,periodo,horas,creditos,categoria,status,descricao,observacoes,oferta\n");
        for(Disciplina d:p.grafo().getDisciplinas()) {
            Object[] campos={d.getCodigo(),d.getNome(),d.getPeriodoReferencia()==0?"":d.getPeriodoReferencia(),d.getCargaHoraria(),d.getCreditos(),d.getCategoria(),d.getStatus().name(),d.getDescricao(),d.getObservacoes(),d.getOferta()};
            List<String> vs=new ArrayList<>();for(Object c:campos)vs.add("\""+(c==null?"":c.toString()).replace("\"","\"\"")+"\"");s.append(String.join(",",vs)).append('\n');
        }
        gravar(path,s.toString());
    }
    static void gravar(Path path,String texto)throws IOException {
        if(texto.getBytes(StandardCharsets.UTF_8).length>8*1024*1024)throw new IOException("Arquivo maior que 8 MiB.");
        Path alvo=path.toAbsolutePath(),tmp=Files.createTempFile(alvo.getParent(),".curriculo-",".tmp");
        try{Files.writeString(tmp,texto,StandardCharsets.UTF_8);try{Files.move(tmp,alvo,StandardCopyOption.ATOMIC_MOVE,StandardCopyOption.REPLACE_EXISTING);}catch(AtomicMoveNotSupportedException e){Files.move(tmp,alvo,StandardCopyOption.REPLACE_EXISTING);}}finally{Files.deleteIfExists(tmp);}
    }
}
