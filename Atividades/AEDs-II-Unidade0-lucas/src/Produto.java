import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public abstract class Produto {
	
	private static final double MARGEM_PADRAO = 0.2;

	/** Código do tipo de produto não perecível no arquivo de dados */
	protected static final int TIPO_NAO_PERECIVEL = 1;

	/** Código do tipo de produto perecível no arquivo de dados */
	protected static final int TIPO_PERECIVEL = 2;

	protected String descricao;
	protected double precoCusto;
	protected double margemLucro;
	
     /**
     * Inicializador privado. Os valores default, em caso de erro, são:
     * "Produto sem descrição", R$ 0.00, 0.0  
     * @param desc Descrição do produto (mínimo de 3 caracteres)
     * @param precoCusto Preço do produto (mínimo 0.01)
     * @param margemLucro Margem de lucro (mínimo 0.01)
     */
	private void init(String desc, double precoCusto, double margemLucro) {
		
		if ((desc.length() >= 3) && (precoCusto > 0.0) && (margemLucro > 0.0)) {
			descricao = desc;
			this.precoCusto = precoCusto;
			this.margemLucro = margemLucro;
		} else {
			throw new IllegalArgumentException("Valores inválidos para os dados do produto.");
		}
	}
	
     /**
     * Construtor completo. Os valores default, em caso de erro, são:
     * "Produto sem descrição", R$ 0.00, 0.0  
     * @param desc Descrição do produto (mínimo de 3 caracteres)
     * @param precoCusto Preço do produto (mínimo 0.01)
     * @param margemLucro Margem de lucro (mínimo 0.01)
     */
	protected Produto(String desc, double precoCusto, double margemLucro) {
		init(desc, precoCusto, margemLucro);
	}
	
     /**
     * Construtor sem margem de lucro - fica considerado o valor padrão de margem de lucro.
     * Os valores default, em caso de erro, são:
     * "Produto sem descrição", R$ 0.00 
     * @param desc Descrição do produto (mínimo de 3 caracteres)
     * @param precoCusto Preço do produto (mínimo 0.01)
     */
	protected Produto(String desc, double precoCusto) {
		init(desc, precoCusto, MARGEM_PADRAO);
	}
	
     /**
     * Retorna o valor de venda do produto, considerando seu preço de custo e margem de lucro.
     * @return Valor de venda do produto (double, positivo)
     */
	public abstract double valorDeVenda();
	
	/**
     * Descrição, em string, do produto, contendo sua descrição e o valor de venda.
     *  @return String com o formato:
     * [NOME]: R$ [VALOR DE VENDA]
     */
    @Override
	public String toString() {
    	
    	NumberFormat moeda = NumberFormat.getCurrencyInstance();
    	
    	return String.format("NOME: " + descricao + ": " + moeda.format(valorDeVenda()));
	}
    
    /**
     * Igualdade de produtos: caso possuam o mesmo nome/descrição. 
     * @param obj Outro produto a ser comparado 
     * @return booleano true/false conforme o parâmetro possua a descrição igual ou não a este produto.
     */
    @Override
    public boolean equals(Object obj) {

        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Produto)) {
            return false;
        }

        Produto outro = (Produto) obj;
        return this.descricao.equalsIgnoreCase(outro.descricao);
    }

    /**
     * Retorna a descrição do produto.
     * @return A descrição do produto.
     */
    public String getDescricao() {
        return descricao;
    }
    
    /**
     * Cria um produto a partir de uma linha de dados em formato texto. A linha de dados deve estar de acordo com a formatação
     * "tipo;descrição;preçoDeCusto;margemDeLucro;[dataDeValidade]"
     * ou o funcionamento não será garantido. Os tipos são 1, para produto não perecível; e 2, para perecível.
     * @param linha Linha com os dados do produto a ser criado.
     * @return Um produto com os dados recebidos
     */
    static Produto criarDoTexto(String linha) {

        String[] dados = linha.split(";");

        int tipo = Integer.parseInt(dados[0].trim());
        String descricao = dados[1].trim();
        double precoCusto = Double.parseDouble(dados[2].trim());
        double margemLucro = Double.parseDouble(dados[3].trim());

        if (tipo == TIPO_PERECIVEL) {
            DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            LocalDate validade = LocalDate.parse(dados[4].trim(), formato);
            return new ProdutoPerecivel(descricao, precoCusto, margemLucro, validade);
        }

        return new ProdutoNaoPerecivel(descricao, precoCusto, margemLucro);
    }
    	
    /**
     * Gera uma linha de texto a partir dos dados do produto.
     * @return Uma string no formato "tipo;descrição;preçoDeCusto;margemDeLucro;[dataDeValidade]"
     */
    public abstract String gerarDadosTexto();
}