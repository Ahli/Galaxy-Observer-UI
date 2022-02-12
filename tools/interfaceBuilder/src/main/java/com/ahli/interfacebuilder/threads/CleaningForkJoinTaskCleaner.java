// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package com.ahli.interfacebuilder.threads;

import java.util.concurrent.ForkJoinPool;

public interface CleaningForkJoinTaskCleaner {
	
	void setExecutor(final ForkJoinPool executor);
	
	void tryCleanUp();
}
