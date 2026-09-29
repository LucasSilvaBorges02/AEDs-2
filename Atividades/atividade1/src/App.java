import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Locale;
import java.util.Random;

/** 
 * MIT License
 *
 * Copyright(c) 2024-255 João Caram <caram@pucminas.br>
 *                       Eveline Alonso Veloso
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

public class App {
    static final int[] TAMANHOS_TESTE_GRANDE =  { 31_250_000, 62_500_000, 125_000_000, 250_000_000, 500_000_000 };
    static final int[] TAMANHOS_TESTE_MEDIO =   {     12_500,     25_000,      50_000,     100_000,     200_000 };
    static final int[] TAMANHOS_TESTE_PEQUENO = {          3,          6,          12,          24,          48 };
    static final double NANO_TO_MILLI = 1.0/1_000_000;
    static Random aleatorio = new Random(42);
    static volatile long operacoes;   // volatile impede o JIT de "encurtar" os lacos ao contar
    static int consumidor;            // apenas guarda os retornos, para o JIT nao descartar as chamadas

    /**
     * Cada caso e repetido ate atingir TEMPO_MINIMO_MS (no maximo MAX_REPETICOES vezes)
     * e o tempo registrado e a media. Casos muito rapidos precisam de varias repeticoes
     * para o relogio fazer sentido; casos longos rodam uma vez so.
     */
    static final int MAX_REPETICOES = 5;
    static final double TEMPO_MINIMO_MS = 100;

    /**
     * Código de teste 1. Este método...
     * @param vetor Vetor com dados para teste.
     * @return Uma resposta que significa....
     */
    static int codigo1(int[] vetor) {
        int resposta = 0;
        for (int i = 0; i < vetor.length; i += 2) {
            operacoes++;                    // operacao relevante: uma iteracao do laco
            resposta += vetor[i] % 2;
        }
        return resposta;
    }

    /**
     * Código de teste 2. Este método...
     * @param vetor Vetor com dados para teste.
     * @return Uma resposta que significa....
     */
    static int codigo2(int[] vetor) {
        int contador = 0;
        for (int k = (vetor.length - 1); k > 0; k /= 2) {
            for (int i = 0; i <= k; i++) {
                operacoes++;                // operacao relevante: uma iteracao do laco interno
                contador++;
            }
        }
        return contador;
    }

    /**
     * Código de teste 3. Este método...
     * @param vetor Vetor com dados para teste.
     */
    static void codigo3(int[] vetor) {
        for (int i = 0; i < vetor.length - 1; i++) {
            int menor = i;
            for (int j = i + 1; j < vetor.length; j++) {
                operacoes++;                // operacao relevante: comparacao entre elementos
                if (vetor[j] < vetor[menor])
                    menor = j;
            }
            int temp = vetor[i];
            vetor[i] = vetor[menor];
            vetor[menor] = temp;
        }
    }

    /**
     * Código de teste 4 (recursivo). Este método...
     * @param n Ponto inicial do algoritmo
     * @return Um inteiro que significa...
     */
    static int codigo4(int n) {
        operacoes++;                        // operacao relevante: uma chamada do metodo
        if (n <= 2)
            return 1;
        else
            return codigo4(n - 1) + codigo4(n - 2);
    }

    /**
     * Gerador de vetores aleatórios de tamanho pré-definido. 
     * @param tamanho Tamanho do vetor a ser criado.
     * @return Vetor com dados aleatórios, com valores entre 1 e (tamanho/2), desordenado.
     */
    static int[] gerarVetor(int tamanho) {
        int[] vetor = new int[tamanho];
        for (int i = 0; i < tamanho; i++) {
            vetor[i] = aleatorio.nextInt(1, tamanho/2);
        }
        return vetor;      
    }

    // ==================================================================
    // INSTRUMENTACAO: execucao dos testes, coleta e gravacao dos dados
    // ==================================================================

    /** Um caso de teste ja executado: tamanho da entrada, operacoes contadas e tempo medio. */
    static class Resultado {
        int n;
        long operacoes;
        double tempoMs;

        Resultado(int n, long operacoes, double tempoMs) {
            this.n = n;
            this.operacoes = operacoes;
            this.tempoMs = tempoMs;
        }
    }

    /** Cabecalho da tabela impressa no console. */
    static void imprimirCabecalho(String titulo) {
        System.out.println();
        System.out.println("=== " + titulo + " ===");
        System.out.printf("%15s | %20s | %14s%n", "n", "operacoes", "tempo (ms)");
        System.out.println("----------------+----------------------+---------------");
    }

    /** Imprime uma linha da tabela assim que o caso termina. */
    static void imprimirLinha(Resultado r) {
        System.out.printf("%15s | %20s | %14s%n",
                numero(r.n), numero(r.operacoes), tempo(r.tempoMs));
    }

    /** Formata inteiros com separador de milhar (1.234.567). */
    static String numero(long valor) {
        return String.format(Locale.US, "%,d", valor).replace(',', '.');
    }

    /** Formata o tempo com virgula decimal, como a planilha em portugues espera. */
    static String tempo(double tempoMs) {
        return String.format(Locale.US, "%.4f", tempoMs).replace('.', ',');
    }

    /**
     * Executa os codigos 1 e 2 (Teste Grande). Os dois nao alteram o vetor,
     * entao o mesmo vetor gerado e reaproveitado nas duas medicoes - isso evita
     * criar duas vezes um vetor que pode ocupar varios GB de memoria.
     */
    static void executarTesteGrande() {
        Resultado[] res1 = new Resultado[TAMANHOS_TESTE_GRANDE.length];
        Resultado[] res2 = new Resultado[TAMANHOS_TESTE_GRANDE.length];
        int quantosDeuCerto = 0;

        imprimirCabecalho("CODIGO 1 e CODIGO 2 - Teste Grande");

        for (int i = 0; i < TAMANHOS_TESTE_GRANDE.length; i++) {
            int n = TAMANHOS_TESTE_GRANDE[i];
            try {
                System.out.println("  (gerando vetor de " + numero(n) + " posicoes...)");
                int[] vetor = gerarVetor(n);

                long somaTempo1 = 0, ops1 = 0;
                int reps1 = 0;
                do {
                    operacoes = 0;
                    long inicio = System.nanoTime();
                    consumidor += codigo1(vetor);
                    somaTempo1 += System.nanoTime() - inicio;
                    ops1 = operacoes;
                    reps1++;
                } while (reps1 < MAX_REPETICOES && somaTempo1 * NANO_TO_MILLI < TEMPO_MINIMO_MS);

                long somaTempo2 = 0, ops2 = 0;
                int reps2 = 0;
                do {
                    operacoes = 0;
                    long inicio = System.nanoTime();
                    consumidor += codigo2(vetor);
                    somaTempo2 += System.nanoTime() - inicio;
                    ops2 = operacoes;
                    reps2++;
                } while (reps2 < MAX_REPETICOES && somaTempo2 * NANO_TO_MILLI < TEMPO_MINIMO_MS);

                res1[i] = new Resultado(n, ops1, somaTempo1 * NANO_TO_MILLI / reps1);
                res2[i] = new Resultado(n, ops2, somaTempo2 * NANO_TO_MILLI / reps2);
                quantosDeuCerto++;

                System.out.print("  codigo1 ->");
                imprimirLinha(res1[i]);
                System.out.print("  codigo2 ->");
                imprimirLinha(res2[i]);

                vetor = null;      // libera a memoria antes do proximo tamanho
                System.gc();
            } catch (OutOfMemoryError e) {
                System.out.println("  !! memoria insuficiente para n = " + numero(n)
                        + ". Rode com mais heap, por exemplo: java -Xmx8g App");
                break;
            }
        }

        salvarCSV("resultados/codigo1.csv", res1, quantosDeuCerto);
        salvarCSV("resultados/codigo2.csv", res2, quantosDeuCerto);
    }

    /** Executa o codigo 3 (Teste Medio). O vetor e regerado a cada repeticao porque a ordenacao o altera. */
    static void executarTesteMedio() {
        Resultado[] resultados = new Resultado[TAMANHOS_TESTE_MEDIO.length];
        imprimirCabecalho("CODIGO 3 - Teste Medio");

        for (int i = 0; i < TAMANHOS_TESTE_MEDIO.length; i++) {
            int n = TAMANHOS_TESTE_MEDIO[i];
            long somaTempo = 0, ops = 0;
            int reps = 0;
            do {
                int[] vetor = gerarVetor(n);   // regerado a cada repeticao: a ordenacao altera o vetor
                operacoes = 0;
                long inicio = System.nanoTime();
                codigo3(vetor);
                somaTempo += System.nanoTime() - inicio;
                ops = operacoes;
                reps++;
            } while (reps < MAX_REPETICOES && somaTempo * NANO_TO_MILLI < TEMPO_MINIMO_MS);

            resultados[i] = new Resultado(n, ops, somaTempo * NANO_TO_MILLI / reps);
            imprimirLinha(resultados[i]);
        }

        salvarCSV("resultados/codigo3.csv", resultados, resultados.length);
    }

    /** Executa o codigo 4 (Teste Pequeno). Nao usa vetor: o parametro e o proprio n. */
    static void executarTestePequeno() {
        Resultado[] resultados = new Resultado[TAMANHOS_TESTE_PEQUENO.length];
        imprimirCabecalho("CODIGO 4 - Teste Pequeno");

        for (int i = 0; i < TAMANHOS_TESTE_PEQUENO.length; i++) {
            int n = TAMANHOS_TESTE_PEQUENO[i];
            long somaTempo = 0, ops = 0;
            int reps = 0;
            do {
                operacoes = 0;
                long inicio = System.nanoTime();
                consumidor += codigo4(n);
                somaTempo += System.nanoTime() - inicio;
                ops = operacoes;
                reps++;
            } while (reps < MAX_REPETICOES && somaTempo * NANO_TO_MILLI < TEMPO_MINIMO_MS);

            resultados[i] = new Resultado(n, ops, somaTempo * NANO_TO_MILLI / reps);
            imprimirLinha(resultados[i]);
        }

        salvarCSV("resultados/codigo4.csv", resultados, resultados.length);
    }

    /** Grava os "quantos" primeiros resultados em CSV (separador ';', decimal com virgula). */
    static void salvarCSV(String caminho, Resultado[] resultados, int quantos) {
        if (quantos == 0) return;
        new File("resultados").mkdirs();

        try (PrintWriter out = new PrintWriter(new FileWriter(caminho))) {
            out.println("n;operacoes;tempo_ms");
            for (int i = 0; i < quantos; i++) {
                Resultado r = resultados[i];
                out.println(r.n + ";" + r.operacoes + ";" + tempo(r.tempoMs));
            }
            System.out.println("-> arquivo gerado: " + caminho);
        } catch (IOException e) {
            System.out.println("Erro ao salvar " + caminho + ": " + e.getMessage());
        }
    }

    /**
     * Ponto de entrada. Sem argumentos, roda os quatro algoritmos.
     * Passando numeros ("java App 3 4"), roda apenas os testes escolhidos:
     *   1 ou 2 -> Teste Grande | 3 -> Teste Medio | 4 -> Teste Pequeno
     */
    public static void main(String[] args) {
        boolean grande = true, medio = true, pequeno = true;

        if (args.length > 0) {
            grande = medio = pequeno = false;
            for (String arg : args) {
                if (arg.equals("1") || arg.equals("2")) grande = true;
                if (arg.equals("3")) medio = true;
                if (arg.equals("4")) pequeno = true;
            }
        }

        System.out.println("AEDs II - Contagem de operacoes e medicao do tempo de execucao");

        long inicioTudo = System.nanoTime();
        if (grande)  executarTesteGrande();
        if (medio)   executarTesteMedio();
        if (pequeno) executarTestePequeno();

        System.out.println();
        System.out.println("Concluido em " + tempo((System.nanoTime() - inicioTudo) * NANO_TO_MILLI / 1000) + " s.");
        System.out.println("Os arquivos .csv estao na pasta 'resultados'.");
        if (consumidor == Integer.MIN_VALUE) System.out.println(consumidor);   // nunca acontece; existe so para o JIT nao apagar as chamadas
    }
}
