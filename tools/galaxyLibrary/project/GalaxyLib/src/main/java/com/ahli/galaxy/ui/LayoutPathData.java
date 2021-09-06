// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com
package com.ahli.galaxy.ui;

import java.util.List;

public record LayoutPathData(List<String> loaded, List<String> notLoaded, List<String> combined) {
}
