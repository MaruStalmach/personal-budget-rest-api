package com.budget.budget_api.transaction;

import com.budget.budget_api.account.Account;
import com.budget.budget_api.account.AccountRepository;
import com.budget.budget_api.transaction.dto.TransactionRequest;
import com.budget.budget_api.transaction.dto.TransactionResponse;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

import static com.budget.budget_api.transaction.TransactionType.INCOME;

@Service
public class TransactionService {

    // extends JpaRepo -> specific data extraction
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
                .orElseThrow(RuntimeException::new);
        return mapToResponse(transaction);
    }

    @Transactional //for rollback
    public TransactionResponse createNewTransaction(TransactionRequest transactionRequest) {

        Account account = accountRepository.findById(transactionRequest.accountId())
                .orElseThrow(RuntimeException::new);
        Transaction newTransaction = new Transaction();


        newTransaction.setAccount(account);
        newTransaction.setAmount(transactionRequest.amount());

        newTransaction.setType(transactionRequest.type());
        if (transactionRequest.type() == INCOME) {
            account.setBalance(account.getBalance().add(transactionRequest.amount()));
        } else {
            account.setBalance(account.getBalance().subtract(transactionRequest.amount()));
        }

        newTransaction.setCategory(transactionRequest.category());
        newTransaction.setDescription(transactionRequest.description());
        newTransaction.setTransactionTime(LocalDateTime.now());

        Transaction savedTransaction = transactionRepository.save(newTransaction);
        accountRepository.save(account);

        return mapToResponse(savedTransaction);
    }


}
