package be.vdab.repositories;

import be.vdab.exceptions.OnvoldoendeSaldoException;
import be.vdab.exceptions.RekeningBestaatAlException;
import be.vdab.exceptions.RekeningNietGevondenException;
import be.vdab.exceptions.VerkeerdRekeningnummerException;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Set;

public class RekeningRepository extends AbstractRepository {
    public void creerEenNieuweBankrekening(String rekening) throws SQLException {
        var cijfer1 = Long.parseLong(rekening.substring(2, 4));
        var cijfer2 = Long.parseLong(rekening.substring(5, 16));
        var controlcijfer = cijfer2 * 1000000 + 1114 * 100 + cijfer1;
        if (rekening.length() != 16 || !rekening.startsWith("BE") || cijfer1 < 2 || cijfer1 > 98 || controlcijfer % 97 != 1) {
            throw new VerkeerdRekeningnummerException();
        }
        var sql = "INSERT INTO rekeningen(nummer) VALUES (?)";
        try (var connection = super.getConnection();
             var statementInsert = connection.prepareStatement(sql)) {
            connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            connection.setAutoCommit(false);
            statementInsert.setString(1, rekening);
            try {
                statementInsert.executeUpdate();
                connection.commit();
            } catch (SQLException ex) {
                try (var statementSelect = connection.prepareStatement(
                        "SELECT nummer FROM rekeningen WHERE nummer = ?")) {
                    statementSelect.setString(1, rekening);
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

    public void overschrijvenDeBankrekeningen(Set<String> rekeningen, BigDecimal bedrag) throws SQLException {
        var naarRekening = "SELECT saldo FROM rekeningen WHERE nummer = ?";
        var vanRekening = "SELECT saldo FROM rekeningen WHERE nummer = ?";
        var vanRekeningUpdate = "UPDATE rekeningen SET saldo=saldo-? WHERE nummer = ?";
        var naarRekeningUpdate = "UPDATE rekeningen SET saldo=saldo+? WHERE nummer = ?";
        try (var connection = super.getConnection()) {
            var statementNaar = connection.prepareStatement(naarRekening);
            connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            connection.setAutoCommit(false);
            statementNaar.setString(1, rekeningen.toArray()[1].toString());
            var resultNaar = statementNaar.executeQuery();
            if (!resultNaar.next()) {
                connection.commit();
                throw new RekeningNietGevondenException();
            }
            try (var statementVan = connection.prepareStatement(vanRekening)) {
                statementVan.setString(1, rekeningen.toArray()[0].toString());
                var resultVan = statementVan.executeQuery();
                if (!resultVan.next()) {
                    connection.commit();
                    throw new RekeningNietGevondenException();
                }
                if (resultVan.getBigDecimal("saldo").compareTo(bedrag) < 0) {
                    connection.commit();
                    throw new OnvoldoendeSaldoException();
                } else {
                    try (var statementVanUpdate = connection.prepareStatement(vanRekeningUpdate)) {
                        statementVanUpdate.setBigDecimal(1, bedrag);
                        statementVanUpdate.setString(2, rekeningen.toArray()[0].toString());
                        statementVanUpdate.executeUpdate();
                    }
                    try (var statementNaarUpdate = connection.prepareStatement(naarRekeningUpdate)) {
                        statementNaarUpdate.setBigDecimal(1, bedrag);
                        statementNaarUpdate.setString(2, rekeningen.toArray()[1].toString());
                        statementNaarUpdate.executeUpdate();
                    }
                    connection.commit();
                }
            }
        }
    }
}

