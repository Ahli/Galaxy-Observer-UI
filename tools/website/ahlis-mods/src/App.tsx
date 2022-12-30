import './App.css';
import { HomePage } from './pages/HomePage';
import '@fontsource/roboto';
import { HashRouter as Router, Route, Routes, Navigate } from 'react-router-dom';
import { Navbar, navBarWidth } from './components/navigation/NavBar';
import { NotFoundPage } from './pages/NotFoundPage';
import { ThemeProvider, createTheme } from '@mui/material/styles';
import { ChangelogPage } from './pages/ChangelogPage';
import { ahliObsChangelog } from './content/ahliObs/changelog/ahliObsChangelog';
import { gh3changelog } from './content/gameheart/v3/changelog/gh3changelog';
import { pro2020AhliModChangelog } from './content/pro2020ahlimod/changelog/pro2020ahliModChangelog';
import { HtmlPage } from './pages/HtmlPage';
import { ahliObsShortcuts } from './content/ahliObs/shortcuts/ahliObsShortcuts';
import { heroesSettings } from './content/heroes/settings';

const darkTheme = createTheme({
  palette: {
    mode: 'dark',
  },
});

function App() {
  return (
    <div className='App'>
      <ThemeProvider theme={darkTheme}>
        <Router>
          <Navbar />
          <div style={{ marginLeft: navBarWidth + 'px' }}>
            <Routes>
              <Route path='/' element={<HomePage />} />
              <Route path='/ahliobs' element={<Navigate to='/ahliobs/changelog' replace />} />
              <Route
                path='/ahliobs/changelog'
                element={<ChangelogPage posts={ahliObsChangelog} title='AhliObs - Changelog' />}
              />
              {
                <Route
                  path='/ahliobs/shortcuts'
                  element={<HtmlPage title='AhliObs - Shortcuts' contentHtml={ahliObsShortcuts} />}
                />
              }
              <Route path='/gameheart' element={<Navigate to='/gameheart/v3/changelog' replace />} />
              <Route
                path='/gameheart/v3/changelog'
                element={<ChangelogPage posts={gh3changelog} title='GameHeart 3 - Changelog' />}
              />
              <Route path='/pro2020ahlimod' element={<Navigate to='/pro2020ahlimod/changelog' replace />} />
              <Route
                path='/pro2020ahlimod/changelog'
                element={<ChangelogPage posts={pro2020AhliModChangelog} title='Pro2020 AhliMod - Changelog' />}
              />
              <Route path='/heroes' element={<Navigate to='/heroes/settings' replace />} />
              <Route
                path='/heroes/settings'
                element={<HtmlPage title='Heroes of the Storm - Settings' contentHtml={heroesSettings} />}
              />
              <Route path='*' element={<NotFoundPage />} />
            </Routes>
          </div>
        </Router>
      </ThemeProvider>
    </div>
  );
}

export default App;
