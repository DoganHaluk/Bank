package be.vdab.repositories;

import be.vdab.exceptions.RekeningBestaatAlException;
import be.vdab.exceptions.RekeningNietGevondenException;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;

public class RekeningRepository extends AbstractRepository {
    public void creerEenNieuweBankrekening(String rekening) throws SQLException {
        var sql = "INSERT INTO rekeningen(nummer) VALUES (?)";
        try (var connection = super.getConnection();
             var statementInsert = connection.prepareStatement(sql)) {
            connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            connection.setAutoCommit(false);
            statementInsert.setObject(1, rekening);
            try {
                statementInsert.executeUpdate();
                connection.commit();
            } catch (SQLException ex) {
                try (var statementSelect = connection.prepareStatement(
                        "SELECT nummer FROM rekeningen WHERE nummer = ?")) {
                    statementSelect.setObject(1, rekening);
                    if (statementSelect.executeQuery().next()) {
                        connection.commit();
                        throw new RekeningBestaatAlException();
                    }
                    connection.commit();
                    throw ex;
                }
            }
        }
    }

    public BigDecimal consulteerHetSaldo(String rekening) throws SQLException {
        var sql = "SELECT saldo FROM rekeningen WHERE nummer = ?";
        try (var connection = super.getConnection();
             var statement = connection.prepareStatement(sql)) {
            connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            connection.setAutoCommit(false);
            statement.setString(1, rekening);
            var result = statement.executeQuery();
            if (result.next()) {
                return result.getBigDecimal("saldo");
            }
            connection.rollback();
            throw new RekeningNietGevondenException();
        }
    }

    public void overschrijvenDeBankrekeningen(String vanRekening, String naarRekening, BigDecimal bedrag) throws SQLException {
        var vanRekeningUpdate = "UPDATE rekeningen SET saldo=saldo-? WHERE nummer = ?";
        var naarRekeningUpdate = "UPDATE rekeningen SET saldo=saldo+? WHERE nummer = ?";
        try (var connection = super.getConnection();
             var statementVanUpdate = connection.prepareStatement(vanRekeningUpdate)) {
            connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            connection.setAutoCommit(false);
            statementVanUpdate.setBigDecimal(1, bedrag);
            statementVanUpdate.setString(2, vanRekening);
            statementVanUpdate.executeUpdate();

            try (var statementNaarUpdate = connection.prepareStatement(naarRekeningUpdate)) {
                statementNaarUpdate.setBigDecimal(1, bedrag);
                statementNaarUpdate.setString(2, naarRekening);
                statementNaarUpdate.executeUpdate();
            }
            connection.commit();
        }
    }
}

