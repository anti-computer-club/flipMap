package com.example.flipmap

import java.util.concurrent.Executor

internal class DirectExecutor : Executor {
    override fun execute(r: Runnable) {
        r.run()
    }
}
