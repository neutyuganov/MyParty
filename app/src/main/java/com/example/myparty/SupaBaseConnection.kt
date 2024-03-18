package com.example.myparty

import android.net.http.HttpResponseCache.install
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.postgrest.Postgrest

class SupaBaseConnection private constructor() {
    companion object {
        @Volatile
        private var instance: SupaBaseConnection? = null

        fun getInstance(): SupaBaseConnection {
            if (instance == null) {
                synchronized(this) {
                    if (instance == null) {
                        instance = SupaBaseConnection()
                    }
                }
            }
            return instance!!
        }
    }

    fun Connect(){
        val sb = createSupabaseClient(
            supabaseUrl = "https://hubifnzlacdcpowjjgra.supabase.co",
            supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Imh1YmlmbnpsYWNkY3Bvd2pqZ3JhIiwicm9sZSI6ImFub24iLCJpYXQiOjE3MTA3NzIzNzEsImV4cCI6MjAyNjM0ODM3MX0.5sX9xsCqOF74XB6pg_85eqjZ9d0bWwQMJ5W4dDN0MU8"
        ) {
            install(Auth)
            install(Postgrest)
            //install other modules
        }
    }
}