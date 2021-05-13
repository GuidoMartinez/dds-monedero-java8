package dds.monedero.model;

import dds.monedero.exceptions.MaximaCantidadDepositosException;
import dds.monedero.exceptions.MaximoExtraccionDiarioException;
import dds.monedero.exceptions.MontoNegativoException;
import dds.monedero.exceptions.SaldoMenorException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Cuenta {
  private double saldo;
  private List<Movimiento> movimientos = new ArrayList<>();

  private static final Integer CANTIDAD_MAXIMA_MOVIMIENTOS = 3;
  private static final Integer MONTO_MAXIMO_EXTRACCION = 1000;

  public Cuenta() {
    this(0);
  }

  public Cuenta(double montoInicial) {
    saldo = montoInicial;
  }

  public void poner(double montoDeposito) {
    validarDeposito(montoDeposito);
    Movimiento deposito = new Deposito(LocalDate.now(), montoDeposito);
    setSaldo(deposito.calcularValor(this));
    movimientos.add(deposito);
  }

  public void sacar(double montoExtraccion) {
    validarExtraccion(montoExtraccion);
    new Extraccion(LocalDate.now(), montoExtraccion).agregateA(this);
  }

  public void agregarMovimiento(Movimiento movimiento) {
    movimientos.add(movimiento);
  }

  public double getMontoExtraidoA(LocalDate fecha) {
    return getMovimientos().stream()
        .filter(movimiento -> !movimiento.isDeposito() && movimiento.getFecha().equals(fecha))
        .mapToDouble(Movimiento::getMonto)
        .sum();
  }

  private void validarDeposito(double montoDeposito) {
    if (montoDeposito <= 0) {
      throw new MontoNegativoException(montoDeposito
          + ": el monto a ingresar debe ser un valor positivo");
    }

    if (getMovimientos().stream().filter(Movimiento::isDeposito).count()
        >= CANTIDAD_MAXIMA_MOVIMIENTOS) {
      throw new MaximaCantidadDepositosException("Ya excedio los "
          + CANTIDAD_MAXIMA_MOVIMIENTOS + " depositos diarios");
    }
  }

  private void validarExtraccion(double montoExtraccion) {
    if (montoExtraccion <= 0) {
      throw new MontoNegativoException(montoExtraccion
          + ": el monto a ingresar debe ser un valor positivo");
    }
    if (getSaldo() - montoExtraccion < 0) {
      throw new SaldoMenorException("No puede sacar mas de " + getSaldo() + " $");
    }
    if (montoExtraccion > getLimiteDisponible()) {
      throw new MaximoExtraccionDiarioException("No puede extraer mas de $ "
          + MONTO_MAXIMO_EXTRACCION
          + " diarios, límite: " + getLimiteDisponible());
    }
  }

  private double getLimiteDisponible() {
    double montoExtraidoHoy = getMontoExtraidoA(LocalDate.now());
    return MONTO_MAXIMO_EXTRACCION - montoExtraidoHoy;
  }

  public List<Movimiento> getMovimientos() {
    return movimientos;
  }

  public double getSaldo() {
    return saldo;
  }

  public void setSaldo(double saldo) {
    this.saldo = saldo;
  }

}
