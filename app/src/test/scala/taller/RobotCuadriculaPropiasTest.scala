package taller

import org.scalatest.funsuite.AnyFunSuite
import org.junit.runner.RunWith
import org.scalatestplus.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class RobotCuadriculaPropiasTest extends AnyFunSuite {

  val r = new RobotCuadricula()
  import r._

  val todas: List[Direccion] = List(Norte, Este, Sur, Oeste)

  // Punto 1 -------------------------------------------------------------------

  test("girar (propia): Este a la derecha da Sur") {
    assert(girar(Este, Derecha) == Sur)
  }

  test("girar (propia): Sur a la derecha da Oeste") {
    assert(girar(Sur, Derecha) == Oeste)
  }

  test("girar (propia): Este a la izquierda da Norte") {
    assert(girar(Este, Izquierda) == Norte)
  }

  test("girar (propia): Oeste a la izquierda da Sur") {
    assert(girar(Oeste, Izquierda) == Sur)
  }

  test("girar (propia): dos giros a la derecha equivalen a dos a la izquierda (media vuelta)") {
    assert(todas.forall(d =>
      girar(girar(d, Derecha), Derecha) == girar(girar(d, Izquierda), Izquierda)))
  }

  test("girar (propia): cuatro giros a la izquierda vuelven a cualquier dirección inicial") {
    assert(todas.forall(d =>
      List(Izquierda, Izquierda, Izquierda, Izquierda)
        .foldLeft(d)((acc, s) => girar(acc, s)) == d))
  }

  test("girar (propia): girar siempre cambia la dirección") {
    assert(todas.forall(d => girar(d, Derecha) != d && girar(d, Izquierda) != d))
  }

  // Punto 2 -------------------------------------------------------------------

  test("ejecutar (propia): avanzar al este aumenta x") {
    assert(ejecutar(Estado(0, 0, Este), Avanzar(3)) == Estado(3, 0, Este))
  }

  test("ejecutar (propia): girar a la izquierda no mueve al robot") {
    assert(ejecutar(inicial, Girar(Izquierda)) == Estado(0, 0, Oeste))
  }

  test("ejecutar (propia): repetir con veces negativas no hace nada") {
    assert(ejecutar(Estado(2, 3, Sur), Repetir(-3, Avanzar(5))) == Estado(2, 3, Sur))
  }

  test("ejecutar (propia): repetir tres veces un avance acumula el desplazamiento") {
    assert(ejecutar(inicial, Repetir(3, Avanzar(2))) == Estado(0, 6, Norte))
  }

  test("ejecutar (propia): tres lados de un cuadrado dejan al robot en otra casilla") {
    val tresLados = Repetir(3, Secuencia(List(Avanzar(1), Girar(Derecha))))
    assert(ejecutar(inicial, tresLados) == Estado(1, 0, Oeste))
  }

  test("ejecutar (propia): secuencias anidadas se ejecutan en orden") {
    val o = Secuencia(List(Avanzar(1), Secuencia(List(Girar(Derecha), Avanzar(2)))))
    assert(ejecutar(inicial, o) == Estado(2, 1, Este))
  }

  test("ejecutar (propia): un rectángulo recorrido dos veces vuelve al origen") {
    val rectangulo = Secuencia(List(Avanzar(3), Girar(Derecha), Avanzar(2), Girar(Derecha)))
    assert(ejecutar(inicial, Repetir(2, rectangulo)) == Estado(0, 0, Norte))
  }

  test("ejecutar (propia): repetir dentro de repetir multiplica las veces") {
    assert(ejecutar(inicial, Repetir(2, Repetir(3, Avanzar(1)))) == Estado(0, 6, Norte))
  }

  // Punto 3 -------------------------------------------------------------------


  // Punto 4 -------------------------------------------------------------------


  // Punto 5 -------------------------------------------------------------------



}