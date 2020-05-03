package me.trollcoding.cdmbotsample.order

import java.util.*

class OrderInfoManager {

    val orders = LinkedList<OrderInfo>()

    fun createId() : Int{
        var id : Int = (orders.size+1)
        while(getOrderInfoById(id) != null){
            id++
        }
        return id
    }

    fun getOrderInfoById(id: Int): OrderInfo? {
        for(orderInfo in orders){
            if(orderInfo.orderId == id){
                return orderInfo
            }
        }
        return null
    }

    fun putOrderInfo(orderInfo: OrderInfo){
        orders.add(orderInfo)
    }

    fun removeOrderInfo(orderInfo: OrderInfo){
        orders.remove(orderInfo)
    }
}