package taller

import org.scalatest.funsuite.AnyFunSuite
import org.junit.runner.RunWith
import org.scalatestplus.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class RobotCuadriculaPropiasTest extends AnyFunSuite {

  val r = new RobotCuadricula()
  import r._

  private val cuadrado: Orden =
    Repetir(4, Secuencia(List(Avanzar(1), Girar(Derecha))))

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

  test("P3 cuantasBasicas: Secuencias vacias anidadas valen cero") {
    assert(cuantasBasicas(Secuencia(List(Secuencia(Nil), Secuencia(Nil)))) == 0)
  }

  test("P3 cuantasBasicas: Repetir anidado multiplica las repeticiones") {
    assert(cuantasBasicas(Repetir(3, Repetir(2, Girar(Derecha)))) == 6)
  }

  test("P3 cuantasBasicas: Repetir anidado con Secuencia adentro") {
    val o = Repetir(3, Repetir(2, Secuencia(List(Avanzar(1), Girar(Izquierda)))))
    // 3 * 2 * (1 + 1) = 12
    assert(cuantasBasicas(o) == 12)
  }

  test("P3 cuantasBasicas: Repetir con cero o menos veces vale cero") {
    assert(cuantasBasicas(Repetir(0, Repetir(2, Avanzar(1)))) == 0)
    assert(cuantasBasicas(Repetir(-3, Secuencia(List(Avanzar(5), Girar(Derecha))))) == 0)
  }

  test("P3 cuantasBasicas: mezcla de basicas y repeticiones en una Secuencia") {
    val o = Secuencia(List(Avanzar(1), Repetir(2, Girar(Izquierda)), Avanzar(3), Girar(Derecha)))
    // 1 + 2 + 1 + 1 = 5
    assert(cuantasBasicas(o) == 5)
  }

  test("P3 profundidad: Repetir sobre una basica vale 2") {
    assert(profundidad(Repetir(9, Girar(Izquierda))) == 2)
  }

  test("P3 profundidad: Secuencia con una Secuencia anidada vale 3") {
    val o = Secuencia(List(Avanzar(1), Secuencia(List(Avanzar(2), Girar(Derecha)))))
    assert(profundidad(o) == 3)
  }

  test("P3 profundidad: toma la rama mas honda") {
    val honda = Repetir(2, Repetir(3, Avanzar(1)))   // profundidad 3
    val o = Secuencia(List(Girar(Derecha), honda))   // 1 + max(1, 3) = 4
    assert(profundidad(o) == 4)
  }

  test("P3 profundidad: Repetir sobre una Secuencia vacia vale 2") {
    assert(profundidad(Repetir(5, Secuencia(Nil))) == 2)
  }

  test("P3 estres: un millon de repeticiones se cuenta sin desbordar la pila") {
    assert(cuantasBasicas(Repetir(1000000, Avanzar(1))) == 1000000)
  }

  test("P3 estres: Secuencia de cien mil ordenes") {
    val larga = Secuencia(List.fill(100000)(Avanzar(1)))
    assert(cuantasBasicas(larga) == 100000)
    assert(profundidad(larga) == 2)
  }

  // Punto 4 -------------------------------------------------------------------

  test("P4 componer: deja las dos ordenes en una Secuencia") {
    val o1 = Repetir(2, Avanzar(1))
    val o2 = Repetir(3, Girar(Derecha))
    assert(componer(o1, o2) == Secuencia(List(o1, o2)))
  }

  test("P4 componer: ejecutar la compuesta es ejecutar una y luego la otra") {
    val o1 = Repetir(2, Avanzar(1))
    val o2 = Secuencia(List(Girar(Izquierda), Avanzar(3)))
    assert(ejecutar(inicial, componer(o1, o2)) == ejecutar(ejecutar(inicial, o1), o2))
    assert(ejecutar(inicial, componer(o1, o2)) == Estado(-3, 2, Oeste))
  }

  test("P4 rutina: con cero veces el robot no se mueve") {
    assert(ejecutar(inicial, rutina(5)(Izquierda)(0)) == inicial)
  }

  test("P4 rutina: tres veces a la derecha termina mirando al Oeste") {
    assert(ejecutar(inicial, rutina(2)(Derecha)(3)) == Estado(2, 0, Oeste))
  }

  test("P4 rutina: cuatro lados de dos casillas a la izquierda vuelven al origen") {
    val cuadradoIzq = (veces: Int) => rutina(2)(Izquierda)(veces)
    assert(ejecutar(inicial, cuadradoIzq(4)) == Estado(0, 0, Norte))
  }

  test("P4 rutina: fijados pasos y giro queda una funcion que espera las veces") {
    val parcial = (veces: Int) => rutina(2)(Derecha)(veces)
    assert(parcial(1) == Repetir(1, Secuencia(List(Avanzar(2), Girar(Derecha)))))
  }

  test("P4 aplicarA: una orden compuesta sobre una lista de un solo estado") {
    val estados = List(Estado(1, 1, Este))
    // el cuadrado devuelve al robot al mismo punto y a la misma direccion
    assert(aplicarA(estados, cuadrado) == estados)
  }

  test("P4 aplicarA: estados con direcciones distintas") {
    val estados = List(Estado(0, 0, Norte), Estado(2, 3, Este), Estado(-1, -1, Sur))
    val esperados = List(Estado(0, 5, Norte), Estado(7, 3, Este), Estado(-1, -6, Sur))
    assert(aplicarA(estados, Avanzar(5)) == esperados)
  }

  test("P4 transformar: reemplaza los giros y respeta la estructura") {
    val o = Repetir(2, Secuencia(List(Avanzar(1), Girar(Derecha))))
    val t = transformar(o, {
      case Girar(_) => Girar(Izquierda)
      case otra     => otra
    })
    assert(t == Repetir(2, Secuencia(List(Avanzar(1), Girar(Izquierda)))))
  }

  test("P4 transformar: recorre Repetir anidado dentro de Secuencia") {
    val o = Secuencia(List(Repetir(2, Avanzar(3)), Girar(Izquierda), Repetir(1, Avanzar(5))))
    val t = transformar(o, {
      case Avanzar(n) => Avanzar(2 * n)
      case otra       => otra
    })
    assert(t == Secuencia(List(Repetir(2, Avanzar(6)), Girar(Izquierda), Repetir(1, Avanzar(10)))))
  }

  test("P4 transformar: con la identidad el arbol queda igual") {
    val o = Secuencia(List(Repetir(2, Avanzar(3)), Girar(Izquierda), Secuencia(Nil)))
    assert(transformar(o, (x: Orden) => x) == o)
  }

  test("P4 transformar: invierte el sentido de todos los giros") {
    val o = Secuencia(List(Avanzar(3), Girar(Izquierda), Girar(Derecha)))
    val t = transformar(o, {
      case Girar(Izquierda) => Girar(Derecha)
      case Girar(Derecha)   => Girar(Izquierda)
      case otra             => otra
    })
    assert(t == Secuencia(List(Avanzar(3), Girar(Derecha), Girar(Izquierda))))
  }

  test("P4 estres: aplicarA sobre cien mil estados") {
    val estados = List.fill(100000)(inicial)
    val resultado = aplicarA(estados, Avanzar(1))
    assert(resultado.size == 100000)
    assert(resultado.forall(e => e == Estado(0, 1, Norte)))
  }

  // Punto 5 -------------------------------------------------------------------



}