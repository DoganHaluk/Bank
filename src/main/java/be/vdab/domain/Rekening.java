package be.vdab.domain;

import be.vdab.exceptions.DezelfdeRekeningnummerException;
import be.vdab.exceptions.OnvoldoendeSaldoException;
import be.vdab.exceptions.VerkeerdRekeningnummerException;

import java.math.BigDecimal;

public class Rekening {
    public String iban;
    public String vanIban;
    public String naarIban;
    public BigDecimal bedrag;
    public BigDecimal saldo;


    public Rekening(String iban) {
        this.iban = iban;
    }

    public Rekening(String vanIban, String naarIban) {
        this.vanIban = vanIban;
        this.naarIban = naarIban;
    }

    public Rekening(BigDecimal bedrag, BigDecimal saldo) {
        this.bedrag = bedrag;
        this.saldo = saldo;
    }

    public void isGeldigeIban(String iban) {
        var controlgetal = Long.parseLong(iban.substring(2, 4));
        if (iban.length() != 16 || !iban.startsWith("BE") || controlgetal < 2 || controlgetal > 98) {
            throw new VerkeerdRekeningnummerException();
        } else if ((Long.parseLong(iban.substring(4, 16)) * 1000000 + 1114 * 100 + controlgetal) % 97 != 1) {
            throw new VerkeerdRekeningnummerException();
        }
    }

    public void isAndersIban(String vanIban, String naarIban) {
        if (vanIban.equals(naarIban)) {
            throw new DezelfdeRekeningnummerException();
        }
    }

    public void isGeldigBedrag(BigDecimal bedrag, BigDecimal saldo) {
        if (saldo.compareTo(bedrag) < 0) {
            throw new OnvoldoendeSaldoException();
        }
    }
}
