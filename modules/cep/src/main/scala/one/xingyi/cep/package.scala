package one.xingyi

package object cep {

  type StringMap = Map[String, String]
  type EventToStringMap = Map[Event,Map[String, String]]
  type ValueFn = EventToStringMap => String
  type WhereFn = StringMap => Boolean
}
