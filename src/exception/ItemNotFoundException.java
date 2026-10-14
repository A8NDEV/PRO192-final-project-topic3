/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package exception;

/**
 *
 * @author ASUS
 */
public class ItemNotFoundException extends BusinessException{
    public ItemNotFoundException(String itemId){
        super("Item ID " + itemId + " not found!");
    }
}
