package dds.monedero.model;

import dds.monedero.exceptions.MaximaCantidadDepositosException;
import dds.monedero.exceptions.MaximoExtraccionDiarioException;
import dds.monedero.exceptions.MontoNegativoException;
import dds.monedero.exceptions.SaldoMenorException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Cuenta {

  private final static Integer cantidadMaximaMovimientos = 3;
  private final static Integer montoMaximoDeExtraccion = 1000;

  private double saldo;
  private List<Movimiento> movimientos = new ArrayList<>();

  public Cuenta() {
    this(0);
  }
  public Cuenta(double montoInicial) {
    saldo = montoInicial;
  }

  public void setMovimientos(List<Movimiento> movimientos) {
    this.movimientos = movimientos;
  }

  public void poner(double montoDeposito) {
    if (montoDeposito <= 0) {
      throw new MontoNegativoException(montoDeposito + ": el monto a ingresar debe ser un valor positivo");
    }

    if (getMovimientos().stream().filter(Movimiento::isDeposito).count() >= cantidadMaximaMovimientos) {
      throw new MaximaCantidadDepositosException("Ya excedio los " + cantidadMaximaMovimientos + " depositos diarios");
    }

    new Extraccion(LocalDate.now(), montoDeposito).agregateA(this);
  }

  public void sacar(double montoExtraccion) {
    if (montoExtraccion <= 0) {
      throw new MontoNegativoException(montoExtraccion + ": el monto a ingresar debe ser un valor positivo");
    }
    if (getSaldo() - montoExtraccion < 0) {
      throw new SaldoMenorException("No puede sacar mas de " + getSaldo() + " $");
    }
    double montoExtraidoHoy = getMontoExtraidoA(LocalDate.now());
    double limite = montoMaximoDeExtraccion - montoExtraidoHoy;
    if (montoExtraccion > limite) {
      throw new MaximoExtraccionDiarioException("No puede extraer mas de $ " + montoMaximoDeExtraccion
          + " diarios, límite: " + limite);
    }
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
