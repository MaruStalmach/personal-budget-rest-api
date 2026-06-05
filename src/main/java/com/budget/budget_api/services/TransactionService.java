package com.budget.budget_api.services;

import com.budget.budget_api.entities.Account;
import com.budget.budget_api.repositories.AccountRepository;
import com.budget.budget_api.common.exception.ResourceNotFoundException;
import com.budget.budget_api.dtos.requests.TransactionRequest;
import com.budget.budget_api.dtos.responses.TransactionResponse;
import com.budget.budget_api.entities.Transaction;
import com.budget.budget_api.repositories.TransactionRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;

    public TransactionService(TransactionRepository transactionRepository, AccountRepository accountRepository) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
    }

    private TransactionResponse mapToResponse(Transaction transaction) {
        return new TransactionResponse(
                transaction.getId(),
                transaction.getAccount().getId(),
                transaction.getAmount(),
                transaction.getType(),
                transaction.getCategory(),
                transaction.getDescription(),
                transaction.getTransactionTime()
        );
    }


    public List<TransactionResponse> getAllTransactions() {
        List<Transaction> transactionList = transactionRepository.findAll();
        return transactionList.stream().map(this::mapToResponse).toList();
    }

    public TransactionResponse getTransactionById(Long id) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("transaction with a given id not found"));
        return mapToResponse(transaction);
    }

    public List<TransactionResponse> getTransactionsByAccountId(Long accountId) {
        if (!accountRepository.existsById(accountId)) {
            throw new ResourceNotFoundException("account with id " + accountId + " not found");
        }
        List<Transaction> transactions = transactionRepository.findByAccountId(accountId);
        return transactions.stream().map(this::mapToResponse).toList();
    }

    public List<TransactionResponse> getFilteredTransactions(LocalDateTime from, LocalDateTime to, String category) {
        List<Transaction> transactions = transactionRepository.findFilteredTransactions(from, to, category);
        return transactions.stream().map(this::mapToResponse).toList();
    }

    @Transactional //for rollback
    public TransactionResponse createNewTransaction(TransactionRequest transactionRequest) {

        Account account = accountRepository.findById(transactionRequest.accountId())
                .orElseThrow(() -> new ResourceNotFoundException("account with a given id not found"));
        Transaction newTransaction = new Transaction();


        newTransaction.setAccount(account);
        newTransaction.setAmount(transactionRequest.amount());

        newTransaction.setType(transactionRequest.type());
        account.applyTransaction(transactionRequest.type(), transactionRequest.amount());

        newTransaction.setCategory(transactionRequest.category());
        newTransaction.setDescription(transactionRequest.description());
        newTransaction.setTransactionTime(LocalDateTime.now());

        Transaction savedTransaction = transactionRepository.save(newTransaction);
        accountRepository.save(account);

        return mapToResponse(savedTransaction);
    }

    @Transactional
    public void deleteTransaction(Long id) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("transaction with a given id not found"));

        Account account = transaction.getAccount();
        account.revertTransaction(transaction.getType(), transaction.getAmount());

        accountRepository.save(account);
        transactionRepository.delete(transaction);

    }

}
