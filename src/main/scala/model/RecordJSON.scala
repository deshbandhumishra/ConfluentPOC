package model

class RecordJSON() {
  private[model] var id = 1

  def this(i:Int){
    this()
    this.id=i
  }

  def getid():Int={
    id
  }

}