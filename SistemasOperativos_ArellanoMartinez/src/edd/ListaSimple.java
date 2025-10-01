/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package edd;

import edd.Nodo;

/**
 *
 * @author guante
 */
public class ListaSimple {
    
    private Nodo head;
    private int size;
    
    public ListaSimple(){}
    
    public boolean isEmpty(){
        return this.head == null;
    }
    
    public void mostrarLista(ListaSimple lista){
        Nodo tmp = lista.head;
        while(tmp != null){
            System.out.print("["+tmp.getData()+"]");
            tmp = tmp.getPnext();
        }
    }
    
    public void insertBegin(Object nodo){
        Nodo temp = new Nodo(nodo);
        if(isEmpty()){
            this.head = temp;
        }else{
            temp.setPnext(this.head);
            this.head = temp;
        }
        size ++;
    }
    
    public void insetFinal(Object nodo){
        Nodo temp = new Nodo(nodo);
        Nodo aux = this.head;
        if(isEmpty()){
            insertBegin(nodo);
        }
        else{
            while(aux.getPnext() != null){
                aux = aux.getPnext();
            }
            aux.setPnext(temp);
            
        }
        size ++;
        
    }
    
    public void deleteBegin(){
        Nodo aux = this.head;
        if(isEmpty()){
            System.out.println("La lista esta vacia");
        }else{
            this.head = aux.getPnext();
            aux.setPnext(null);
            size --;
        }
    }
    
    public void deleteFinal(){
        Nodo aux = this.head;
        if(isEmpty()){
            System.out.println("La lista esta vacia");
        }
        else{
           while(aux.getPnext().getPnext() != null){
               aux = aux.getPnext();
           }
           aux.setPnext(null);
           size--;
           
        }
    }
      
    public int sizeLista(){
        return this.size;
    }
    
    public boolean search(Object dato) {
        Nodo temp = this.head;
        while (temp != null) {
            if (temp.getData().equals(dato)) {
                return true; 
            }
            temp = temp.getPnext();
        }
        return false; 
}

}
