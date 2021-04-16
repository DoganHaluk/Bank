package be.vdab;

import be.vdab.domain.Rekening;
import be.vdab.exceptions.*;
import be.vdab.repositories.RekeningRepository;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Scanner;

class Main {
    public static void main(String[] args) {
        var keuze = kiesTransactie();
        while (keuze != 4) {
            switch (keuze) {
                case 1:
                    System.out.println("Tik een nieuw bankrekeningnummer om een nieuwe rekening aan te maken:");
                    var scanner = new Scanner(System.in);
                    var iban = scanner.nextLine();
                    var rekening1 = new Rekening(iban);
                    var repository1 = new RekeningRepository();
                    try {
                        rekening1.isGeldigeIban(iban);
                        repository1.creerEenNieuweBankrekening(iban);
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
                    iban = scanner.nextLine();
                    var repository2 = new RekeningRepository();
                    try {
                        var saldo = repository2.consulteerHetSaldo(iban);
                        System.out.println("Het saldo is: " + saldo);
                    } catch (SQLException ex) {
                        ex.printStackTrace(System.err);
                    } catch (RekeningNietGevondenException ex) {
                        System.out.println("Rekening niet gevonden.");
                    }
                    break;
                case 3:
                    System.out.println("Tik op een bankrekeningnummer waarvan u wilt overschrijven:");
                    scanner = new Scanner(System.in);
                    var vanRekening = scanner.nextLine();
                    var repository3 = new RekeningRepository();
                    try {
                        var vanSaldo = repository3.consulteerHetSaldo(vanRekening);

                        System.out.println("Tik op een bankrekeningnummer waarnaar u wilt overschrijven:");
                        var naarRekening = scanner.nextLine();
                        var rekening2 = new Rekening(vanRekening, naarRekening);
                        try {
                            rekening2.isAndersIban(vanRekening, naarRekening);
                            repository3.consulteerHetSaldo(naarRekening);

                            System.out.println("Tik op het over te boeken bedrag:");
                            var bedrag = scanner.nextBigDecimal();
                            while (bedrag.compareTo(BigDecimal.ZERO) <= 0) {
                                System.out.print("Bedrag moet positief zijn, probeer opnieuw:");
                                bedrag = scanner.nextBigDecimal();
                            }

                            var rekening3 = new Rekening(bedrag, vanSaldo);
                            var repository4 = new RekeningRepository();
                            try {
                                rekening3.isVoldoendeSaldo(bedrag, vanSaldo);
                                repository4.overschrijvenDeBankrekeningen(vanRekening, naarRekening, bedrag);
                                System.out.println("Overdracht heeft plaatsgevonden.");
                            } catch (SQLException ex) {
                                ex.printStackTrace(System.err);
                            } catch (OnvoldoendeSaldoException ex) {
                                System.out.println("Saldo is onvoldoende.");
                            }
                        } catch (SQLException ex) {
                            ex.printStackTrace(System.err);
                        } catch (DezelfdeRekeningnummerException ex) {
                            System.out.println("Rekeningen zijn dezelfde.");
                        } catch (RekeningNietGevondenException ex) {
                            System.out.println("Rekening niet gevonden.");
                        }
                    } catch (SQLException ex) {
                        ex.printStackTrace(System.err);
                    } catch (RekeningNietGevondenException ex) {
                        System.out.println("Rekening niet gevonden.");
                    }
                    break;
            }
            keuze = kiesTransactie();
        }
    }

    private static int kiesTransactie() {
        System.out.println("\nKies een transactie:");
        System.out.println("1. Nieuwe rekening\n" +
                "2. Saldo consulteren\n" +
                "3. Overschrijven\n" +
                "4. Stop");
        var scanner = new Scanner(System.in);
        var transactie = scanner.nextInt();
        while (transactie < 1 || transactie > 4) {
            System.out.println("Kies een geldig nummer!");
            transactie = scanner.nextInt();
        }
        return transactie;
    }
}
