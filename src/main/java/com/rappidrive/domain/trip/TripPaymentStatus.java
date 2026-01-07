package com.rappidrive.domain.trip;

/**
 * Status do pagamento de uma corrida.
 * Representa o estado atual do processamento financeiro da corrida.
 */
public enum TripPaymentStatus {
    /**
     * Pagamento ainda não foi processado.
     * Estado inicial após criação da corrida.
     */
    PENDING,

    /**
     * Pagamento está sendo processado no momento.
     * Gateway de pagamento está processando a transação.
     */
    PROCESSING,

    /**
     * Pagamento foi confirmado com sucesso.
     * Valor foi debitado/recebido e corrida está quitada.
     */
    PAID,

    /**
     * Tentativa de pagamento falhou.
     * Pode requerer nova tentativa ou método alternativo.
     */
    PAYMENT_FAILED,

    /**
     * Pagamento foi reembolsado.
     * Valor foi devolvido ao passageiro.
     */
    REFUNDED;

    /**
     * Verifica se o status representa um estado final do pagamento.
     * Estados finais: PAID, PAYMENT_FAILED, REFUNDED.
     *
     * @return true se o status é final
     */
    public boolean isFinal() {
        return this == PAID || this == PAYMENT_FAILED || this == REFUNDED;
    }

    /**
     * Verifica se é possível processar pagamento a partir deste status.
     * Apenas estados PENDING e PAYMENT_FAILED permitem (re)processamento.
     *
     * @return true se pode processar pagamento
     */
    public boolean canProcessPayment() {
        return this == PENDING || this == PAYMENT_FAILED;
    }

    /**
     * Verifica se é possível reembolsar a partir deste status.
     * Apenas PAID permite reembolso.
     *
     * @return true se pode reembolsar
     */
    public boolean canRefund() {
        return this == PAID;
    }

    /**
     * Verifica se o pagamento foi bem-sucedido.
     *
     * @return true se status é PAID
     */
    public boolean isSuccessful() {
        return this == PAID;
    }
}
