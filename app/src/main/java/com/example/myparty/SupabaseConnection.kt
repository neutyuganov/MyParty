package com.example.myparty

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.postgrest.Postgrest


class SupabaseConnection {
    object Singleton {
        val sb = createSupabaseClient(
            supabaseUrl = "https://utxgimyvkpshrcmstjvo.supabase.co",
            supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InV0eGdpbXl2a3BzaHJjbXN0anZvIiwicm9sZSI6ImFub24iLCJpYXQiOjE3MTMzMzI5ODYsImV4cCI6MjAyODkwODk4Nn0.76KIKvV7rhhdQpESjyqkUdXOC02qD820N7AgK-xPqUU"
        ) {
            install(Auth)
            install(Postgrest)
        }
    }
}