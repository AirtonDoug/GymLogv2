package com.example.gymlog.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.gymlog.ui.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(viewModel: AuthViewModel, navController: NavController) {
    var email by remember { mutableStateOf("") }
    var isEmailValid by remember { mutableStateOf(true) }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Cabeçalho
        Text(
            text = "Recuperar Senha",
            fontSize = 24.sp,
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Campo de Email
        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
                // Validação de email em tempo real
                isEmailValid = it.isEmpty() || android.util.Patterns.EMAIL_ADDRESS.matcher(it).matches()
            },
            label = { Text("Digite seu email") },
            isError = !isEmailValid,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            singleLine = true,
            // AQUI ESTÁ A CORREÇÃO
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = if (isEmailValid) MaterialTheme.colorScheme.primary else Color.Red,
                unfocusedIndicatorColor = if (isEmailValid) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f) else Color.Red,
                errorIndicatorColor = Color.Red,
                errorLabelColor = Color.Red,
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                errorContainerColor = Color.Transparent
            )
        )

        if (!isEmailValid) {
            Text(
                text = "Por favor, insira um email válido.",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp).fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Botão de Recuperação
        Button(
            onClick = {
                if (isEmailValid && email.isNotEmpty()) {
                    viewModel.resetPassword(email) { success ->
                        if (success) {
                            Toast.makeText(context, "Email de recuperação enviado!", Toast.LENGTH_LONG).show()
                            navController.navigate("login")
                        } else {
                            Toast.makeText(context, "Erro ao enviar email", Toast.LENGTH_LONG).show()
                        }
                    }
                } else {
                    Toast.makeText(context, "Insira um email válido", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(8.dp),
            enabled = isEmailValid && email.isNotEmpty()
        ) {
            Text("Enviar Email de Recuperação", fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Botão para Voltar ao Login
        TextButton(
            onClick = { navController.navigate("login") },
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text("Voltar ao Login", fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
        }
    }
}