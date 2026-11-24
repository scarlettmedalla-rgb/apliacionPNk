package com.example.myapplication

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.myapplication.db.DatabaseHelper
import com.example.myapplication.model.User
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SqliteLoginVerificationTest {

    @Test
    fun onlyRegisteredUsersInSqliteCanLogin() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val dbHelper = DatabaseHelper(context)

        // 1. Usuario NO registrado en SQLite debe ser rechazado
        assertNull(dbHelper.findUserByIdentifier("usuario.inexistente@correo.com"))
        val (unknownOk, _, unknownUser) = dbHelper.validateLocalLogin("usuario.inexistente@correo.com", "Clave123!")
        assertFalse(unknownOk)
        assertNull(unknownUser)

        // 2. Usuario semilla con contraseña incorrecta debe ser rechazado
        val (wrongSeedOk, _, _) = dbHelper.validateLocalLogin("scarlett.williams", "Incorrecta1!")
        assertFalse(wrongSeedOk)

        // 3. Usuario semilla con username y con email y contraseña correcta debe ingresar
        val (seedByUsernameOk, seedBlocked1, seedUser1) = dbHelper.validateLocalLogin("scarlett.williams", "Clave123!")
        assertTrue(seedByUsernameOk)
        assertFalse(seedBlocked1)
        assertNotNull(seedUser1)

        val (seedByEmailOk, seedBlocked2, seedUser2) = dbHelper.validateLocalLogin("scarlett.williams@empresa.com", "Clave123!")
        assertTrue(seedByEmailOk)
        assertFalse(seedBlocked2)
        assertNotNull(seedUser2)

        // 4. Registrar un usuario nuevo en SQLite y verificar acceso solo con sus credenciales exactas
        val testEmail = "test.sqlite.verify@correo.com"
        val testUsername = "test.sqlite"
        val testPass = "Segura123!"
        val insertedId = dbHelper.insertUser(
            User(
                rut = "12.345.678-9",
                firstname = "Prueba",
                lastname = "Sqlite",
                email = testEmail,
                phone = "",
                password = testPass,
                username = testUsername
            ),
            syncStatus = 0
        )
        assertTrue("El usuario de prueba debe insertarse en SQLite", insertedId > 0L)

        try {
            // Contraseña incorrecta rechazada
            val (badPassOk, _, _) = dbHelper.validateLocalLogin(testEmail, "OtraClave9!")
            assertFalse(badPassOk)

            // Ingreso válido por correo
            val (emailOk, emailBlocked, foundByEmail) = dbHelper.validateLocalLogin(testEmail, testPass)
            assertTrue(emailOk)
            assertFalse(emailBlocked)
            assertEquals("Prueba", foundByEmail?.firstname)

            // Ingreso válido por username
            val (userOk, userBlocked, foundByUsername) = dbHelper.validateLocalLogin(testUsername, testPass)
            assertTrue(userOk)
            assertFalse(userBlocked)
            assertEquals(testEmail, foundByUsername?.email)

            // Cuenta bloqueada debe quedar marcada como bloqueada
            assertTrue(dbHelper.blockUserByEmail(testEmail))
            val blockedUser = dbHelper.findUserByIdentifier(testEmail)
            assertNotNull(blockedUser)
            assertTrue(blockedUser!!.isBlocked)

            // Reactivación y cambio de clave
            assertTrue(dbHelper.unblockUserAndResetPassword(testEmail, "NuevaClave1!"))
            val (oldPassOk, _, _) = dbHelper.validateLocalLogin(testEmail, testPass)
            assertFalse(oldPassOk)
            val (newPassOk, newBlocked, _) = dbHelper.validateLocalLogin(testEmail, "NuevaClave1!")
            assertTrue(newPassOk)
            assertFalse(newBlocked)
        } finally {
            dbHelper.deleteUser(insertedId.toInt())
        }
    }
}
