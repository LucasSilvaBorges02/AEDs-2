import java.io.File;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.Scanner;

public class App {

    /** Quantidade máxima de produtos que podem ser armazenados no vetor */
    static final int MAX_NOVOS_PRODUTOS = 10;

    /** Nome do arquivo de dados. O arquivo deve estar localizado na raiz do projeto */
    static String nomeArquivoDados;
    
    /** Scanner para leitura de dados do teclado */
    static Scanner teclado;

    /** Vetor de produtos cadastrados */
    static Produto[] produtosCadastrados;

    /** Quantidade de produtos cadastrados atualmente no vetor */
    static int quantosProdutos = 0;

    /** Gera um efeito de pausa na CLI. Espera por um enter para continuar */
    static void pausa() {
        System.out.println("Digite enter para continuar...");
        teclado.nextLine();
    }

    /** Cabeçalho principal da CLI do sistema */
    static void cabecalho() {
        System.out.println("AEDs II COMÉRCIO DE COISINHAS");
        System.out.println("=============================");
    }
    
    /** Imprime o menu principal, lê a opção do usuário e a retorna (int).
     * @return Um inteiro com a opção do usuário.
    */
    static int menu() {
        cabecalho();
        System.out.println("1 - Listar todos os produtos");
        System.out.println("2 - Procurar e imprimir os dados de um produto");
        System.out.println("3 - Cadastrar novo produto");
        System.out.println("0 - Sair");
        System.out.print("Digite sua opção: ");
        return Integer.parseInt(teclado.nextLine());
    }
    
    /**
     * Lê os dados de um arquivo-texto e retorna um vetor de produtos. Arquivo-texto no formato
     * N (quantidade de produtos) <br/>
     * tipo;descrição;preçoDeCusto;margemDeLucro;[dataDeValidade] <br/>
     * Deve haver uma linha para cada um dos produtos. Retorna um vetor vazio em caso de problemas com o arquivo.
     * @param nomeArquivoDados Nome do arquivo de dados a ser aberto.
     * @return Um vetor com os produtos carregados, ou vazio em caso de problemas de leitura.
     */
    static Produto[] lerProdutos(String nomeArquivoDados) {

        Scanner arquivo = null;

        try {
            arquivo = new Scanner(new File(nomeArquivoDados), Charset.forName("UTF-8"));

            int quantidade = Integer.parseInt(arquivo.nextLine().trim());
            Produto[] produtos = new Produto[quantidade + MAX_NOVOS_PRODUTOS];

            quantosProdutos = 0;
            for (int i = 0; i < quantidade; i++) {
                String linha = arquivo.nextLine().trim();
                if (!linha.isEmpty()) {
                    produtos[quantosProdutos] = Produto.criarDoTexto(linha);
                    quantosProdutos++;
                }
            }

            return produtos;

        } catch (Exception erro) {
            System.out.println("Problema na leitura do arquivo de dados: " + erro.getMessage());
            quantosProdutos = 0;
            return new Produto[MAX_NOVOS_PRODUTOS];

        } finally {
            if (arquivo != null) {
                arquivo.close();
            }
        }
    }
    
    /** Localiza um produto no vetor de produtos cadastrados, a partir do nome de produto informado pelo usuário, e imprime seus dados. 
     *  A busca não é sensível ao caso. Em caso de não encontrar o produto, imprime uma mensagem padrão */
    static void localizarProdutos() {

        cabecalho();
        System.out.print("Digite o nome do produto que deseja localizar: ");
        String nomeProcurado = teclado.nextLine();

        boolean encontrado = false;
        for (int i = 0; i < quantosProdutos; i++) {
            if (produtosCadastrados[i].getDescricao().equalsIgnoreCase(nomeProcurado)) {
                System.out.println(produtosCadastrados[i]);
                encontrado = true;
            }
        }

        if (!encontrado) {
            System.out.println("Produto não encontrado.");
        }
    }
    
    /**
     * Salva os dados dos produtos cadastrados no arquivo csv informado. Sobrescreve todo o conteúdo do arquivo.
     * @param nomeArquivo Nome do arquivo a ser gravado.
     */
    public static void salvarProdutos(String nomeArquivo) {

        PrintWriter arquivo = null;

        try {
            arquivo = new PrintWriter(nomeArquivo, Charset.forName("UTF-8"));

            arquivo.println(quantosProdutos);
            for (int i = 0; i < quantosProdutos; i++) {
                arquivo.println(produtosCadastrados[i].gerarDadosTexto());
            }

        } catch (Exception erro) {
            System.out.println("Problema na gravação do arquivo de dados: " + erro.getMessage());

        } finally {
            if (arquivo != null) {
                arquivo.close();
            }
        }
    }
    
    /** Lista todos os produtos cadastrados, numerados, um por linha */
    static void listarTodosOsProdutos() {
    	
    }
    
    /**
     * Rotina para cadastro de um novo produto: pergunta ao usuário o tipo do produto, lê os dados correspondentes,
     * cria o objeto adequado de acordo com o tipo, inclui o produto no vetor.
     */
    static void cadastrarProduto() {
    	
    }  
    
	public static void main(String[] args) {
		teclado = new Scanner(System.in, Charset.forName("UTF-8"));
        nomeArquivoDados = "dadosProdutos.csv";
        produtosCadastrados = lerProdutos(nomeArquivoDados);
        
        int opcao = -1;
      
        do{
            opcao = menu();
            switch (opcao) {
                case 1 -> listarTodosOsProdutos();
                case 2 -> localizarProdutos();
                case 3 -> cadastrarProduto();
            }
            pausa();
        }while(opcao != 0);       

        salvarProdutos(nomeArquivoDados);
        teclado.close();    
    }
}
