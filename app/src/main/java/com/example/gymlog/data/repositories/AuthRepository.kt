package com.example.gymlog.data.repositories

import android.content.Context
import android.util.Log
import com.example.gymlog.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions // <-- IMPORTAÇÃO ADICIONADA
import kotlinx.coroutines.tasks.await

class AuthRepository {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    suspend fun registerUser(email: String, password: String, name: String): Boolean {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val uuid = result.user?.uid
            if (uuid != null) {
                val user = hashMapOf(
                    "uuid" to uuid,
                    "name" to name,
                    "email" to email,
                    "createdAt" to System.currentTimeMillis()
                )
                firestore.collection("users").document(uuid).set(user).await()
            }
            true
        } catch (e: Exception) {
            Log.e("AuthRepository", "Error registering user", e)
            false
        }
    }

    suspend fun loginUser(email: String, password: String): Boolean {
        return try {
            auth.signInWithEmailAndPassword(email, password).await()
            true
        } catch (e: Exception) {
            Log.e("AuthRepository", "Error logging in user", e)
            false
        }
    }

    suspend fun resetPassword(email: String): Boolean {
        return try {
            auth.sendPasswordResetEmail(email).await()
            true
        } catch (e: Exception) {
            Log.e("AuthRepository", "Error resetting password", e)
            false
        }
    }

    suspend fun getUserName(): String? {
        return try {
            val uid = auth.currentUser?.uid
            if (uid != null) {
                val snapshot = firestore.collection("users").document(uid).get().await()
                snapshot.getString("name")
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("AuthRepository", "Error getting user name", e)
            null
        }
    }

    suspend fun getUserPhotoUrl(): String? {
        return try {
            val uid = auth.currentUser?.uid
            if (uid != null) {
                val snapshot = firestore.collection("users").document(uid).get().await()
                snapshot.getString("photoUrl")
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("AuthRepository", "Error getting user photo URL", e)
            null
        }
    }

    fun getGoogleSignInClient(context: Context): GoogleSignInClient {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        return GoogleSignIn.getClient(context, gso)
    }

    // VERSÃO CORRIGIDA E MAIS ROBUSTA DA FUNÇÃO
    suspend fun loginWithGoogle(idToken: String): Boolean {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = auth.signInWithCredential(credential).await()
            val user = result.user

            user?.let {
                val uid = it.uid
                val name = it.displayName ?: "Usuário"
                val email = it.email ?: ""
                val photoUrl = it.photoUrl?.toString() // URL da foto pode ser nulo

                val userRef = firestore.collection("users").document(uid)

                // Dados a serem salvos ou atualizados
                val userData = hashMapOf(
                    "uuid" to uid,
                    "name" to name,
                    "email" to email
                )
                // Adiciona a foto apenas se ela existir
                photoUrl?.let {
                    userData["photoUrl"] = it
                }

                // Usa SetOptions.merge() para criar o usuário se ele não existir,
                // ou atualizar os campos (nome, email, foto) se ele já existir,
                // sem sobrescrever outros dados como 'createdAt'.
                userRef.set(userData, SetOptions.merge()).await()
            }
            true
        } catch (e: Exception) {
            Log.e("AuthRepository", "Error logging in with Google", e)
            false
        }
    }

    fun logout() {
        auth.signOut()
    }

    fun isUserLoggedIn(): Boolean {
        return auth.currentUser != null
    }
}
