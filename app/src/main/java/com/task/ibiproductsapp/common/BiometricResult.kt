package com.task.ibiproductsapp.common

sealed class BiometricResult {
    object Success : BiometricResult()
    object Cancelled : BiometricResult()
    data class Error(val message: String) : BiometricResult()
    object NotAvailable : BiometricResult()
    object NotEnrolled : BiometricResult()
}