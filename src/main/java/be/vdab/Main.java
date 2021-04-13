package be.vdab;

import be.vdab.exceptions.OnvoldoendeSaldoException;
import be.vdab.exceptions.RekeningBestaatAlException;
import be.vdab.exceptions.RekeningNietGevondenException;
import be.vdab.exceptions.VerkeerdRekeningnummerException;
import be.vdab.repositories.RekeningRepository;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.LinkedHashSet;
import java.util.Scanner;

class Main {
    public static void main(String[] args) {
        System.out.println("Kies een transactie:");
        System.out.println(
                "1. Nieuwe rekening\n" +
                        "2. Saldo consulteren\n" +
                        "3. Overschrijven\n" +
                        "4. Stop");
        var scanner = new Scanner(System.in);
        var transactie = scanner.nextInt();
        while (transactie < 1 || transactie > 4) {
            System.out.println("Kies een geldig nummer!");
            transactie = scanner.nextInt();
        }
        switch (transactie) {
            case 1:
                System.out.println("Tik een nieuw bankrekeningnummer om een nieuwe rekening aan te maken:");
                scanner = new Scanner(System.in);
                var rekening = scanner.nextLine();
                var repository1 = new RekeningRepository();
                try {
                    repository1.creerEenNieuweBankrekening(rekening);
                    System.out.println("Er wordt een nieuwe bankrekening aangemaakt.");
                } catch (SQLException ex) {
                    ex.printStackTrace(System.err);
                } catch (VerkeerdRekeningnummerException ex) {
                    System.out.println("Rekeningnummer is verkeerd.");
                } catch (RekeningBestaatAlException ex) {
                    System.out.println("Bankrekening bestaat al.");
                }
                break;
            case 2:
                System.out.println("Tik een bankrekeningnummer om het saldo te consulteren:");
                scanner = new Scanner(System.in);
                rekening = scanner.nextLine();
                var repository2 = new RekeningRepository();
                try {
                    var saldo = repository2.consulteerHetSaldo(rekening);
                    System.out.println("Het saldo is: " + saldo);
                } catch (SQLException ex) {
                    ex.printStackTrace(System.err);
                } catch (RekeningNietGevondenException ex) {
                    System.out.println("Rekening niet gevonden.");
                }
                break;
            case 3:
                var rekeningen = new LinkedHashSet<String>();
                System.out.println("Tik op een van-bankrekeningnummer en een naar-bankrekeningnummer:");
                var scanner1 = new Scanner(System.in);
                var index = 0;
                while (index < 2) {
                    rekening = scanner1.nextLine();
                    if (!rekeningen.add(rekening)) {
                        System.out.print(rekening + " reeds getypt, probeer opnieuw:\n");
                    } else {
                        index++;
                    }
                }
                System.out.println("Tik op het over te boeken bedrag:");
                var scanner2 = new Scanner(System.in);
                var bedrag = scanner2.nextBigDecimal();
                while (bedrag.compareTo(BigDecimal.ZERO) <= 0) {
                    System.out.print("Bedrag moet positief zijn, probeer opnieuw:");
                    scanner2 = new Scanner(System.in);
                    bedrag = scanner2.nextBigDecimal();
                }
                ;
                var repository3 = new RekeningRepository();
                try {
                    repository3.overschrijvenDeBankrekeningen(rekeningen, bedrag);
                    System.out.println("Overdracht heeft plaatsgevonden.");
                } catch (SQLException ex) {
                    ex.printStackTrace(System.err);
                } catch (RekeningNietGevondenException ex) {
                    System.out.println("Rekening niet gevonden.");
                } catch (OnvoldoendeSaldoException ex) {
                    System.out.println("Saldo is onvoldoende.");
                }
                break;
            case 4:
                break;
            default:
                System.out.println("Geen keuze 1 tot 3 gegeven");
                break;
        }
    }
}
