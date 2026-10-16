import { useEffect, useState } from 'react'
import './App.css'

function App() {
  const [activeSection, setActiveSection] = useState('dashboard')
  const [isAddExpenseOpen, setIsAddExpenseOpen] = useState(false)
  const [newExpenseDescription, setNewExpenseDescription] = useState('')
  const [newExpenseAmount, setNewExpenseAmount] = useState('')
  const [newExpensePaidBy, setNewExpensePaidBy] = useState('1')
  const [newExpenseCategory, setNewExpenseCategory] = useState('GROCERIES')
  const [newExpenseDate, setNewExpenseDate] = useState(new Date().toISOString().slice(0, 10))
  const [newExpenseSplits, setNewExpenseSplits] = useState<Record<string, boolean>>({ '1': true, '2': true, '3': true })
  const [expenseSubmitting, setExpenseSubmitting] = useState(false)
  const [expenseSubmitError, setExpenseSubmitError] = useState('')

  const [expenses, setExpenses] = useState<{
    id: number
    amount: number
    description?: string
    title?: string
    category?: string
    date?: string
  }[]>([])
  const [expensesLoading, setExpensesLoading] = useState(true)
  const [expensesError, setExpensesError] = useState(false)

  const [balances, setBalances] = useState<Record<string, number | string>>({})
  const [balancesLoading, setBalancesLoading] = useState(true)
  const [balancesError, setBalancesError] = useState(false)

  useEffect(() => {
    fetch('http://localhost:8080/api/expenses')
      .then((response) => {
        if (!response.ok) {
          throw new Error('Could not load expenses')
        }
        return response.json()
      })
      .then((data: {
        id: number
        amount: number
        description?: string
        title?: string
        category?: string
        date?: string
      }[]) => {
        setExpenses(data)
        setExpensesLoading(false)
      })
      .catch(() => {
        setExpensesError(true)
        setExpensesLoading(false)
      })
  }, [])

  useEffect(() => {
    fetch('http://localhost:8080/api/expenses/balances')
      .then((response) => {
        if (!response.ok) {
          throw new Error('Could not load balances')
        }
        return response.json()
      })
      .then((data: Record<string, number | string>) => {
        setBalances(data)
        setBalancesLoading(false)
      })
      .catch(() => {
        setBalancesError(true)
        setBalancesLoading(false)
      })
  }, [])

  const totalSpending = expenses.reduce((total, expense) => total + expense.amount, 0)
  const dogaBalance = Number(balances['Doga'] ?? 0)

  const handleAddExpense = async (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    setExpenseSubmitting(true)
    setExpenseSubmitError('')

    const selectedUserIds = Object.entries(newExpenseSplits)
      .filter(([, selected]) => selected)
      .map(([userId]) => Number(userId))

    if (selectedUserIds.length === 0) {
      setExpenseSubmitError('Select at least one housemate to split this expense with.')
      setExpenseSubmitting(false)
      return
    }

    const amount = Number(newExpenseAmount)
    const splitAmount = Math.round((amount / selectedUserIds.length) * 100) / 100
    let allocated = 0
    const splits = selectedUserIds.map((userId, index) => {
      const amountOwed = index === selectedUserIds.length - 1
        ? Math.round((amount - allocated) * 100) / 100
        : splitAmount
      allocated = Math.round((allocated + amountOwed) * 100) / 100
      return { userId, amountOwed }
    })

    try {
      const response = await fetch('http://localhost:8080/api/expenses', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          description: newExpenseDescription.trim(),
          amount,
          paidById: Number(newExpensePaidBy),
          householdId: 1,
          category: newExpenseCategory,
          date: newExpenseDate,
          splits,
        }),
      })

      if (!response.ok) {
        const message = await response.text()
        throw new Error(message || 'Could not save the expense. Please check the details and try again.')
      }

      const expensesResponse = await fetch('http://localhost:8080/api/expenses')
      if (!expensesResponse.ok) throw new Error('Expense saved, but the expense list could not refresh. Refresh the page to see it.')
      const expensesData = await expensesResponse.json()
      setExpenses(expensesData)

      const balancesResponse = await fetch('http://localhost:8080/api/expenses/balances')
      if (balancesResponse.ok) {
        const balancesData = await balancesResponse.json()
        setBalances(balancesData)
      }

      setIsAddExpenseOpen(false)
      setNewExpenseDescription('')
      setNewExpenseAmount('')
      setNewExpensePaidBy('1')
      setNewExpenseCategory('GROCERIES')
      setNewExpenseDate(new Date().toISOString().slice(0, 10))
      setNewExpenseSplits({ '1': true, '2': true, '3': true })
    } catch (error) {
      setExpenseSubmitError(error instanceof Error ? error.message : 'Something went wrong while saving the expense.')
    } finally {
      setExpenseSubmitting(false)
    }
  }

  return (
    <div className="app-shell">
      <header className="topbar">
        <a className="brand" href="#home" aria-label="FlatMate home">
          <span className="brand-mark">f.</span>
          <span>flatmate<span className="brand-period">.</span></span>
        </a>

        <nav className="top-navigation" aria-label="Main navigation">
          <a
            className={`nav-link ${activeSection === 'dashboard' ? 'active' : ''}`}
            href="#dashboard"
            onClick={() => setActiveSection('dashboard')}
          >
            Dashboard
          </a>
          <a
            className={`nav-link ${activeSection === 'expenses' ? 'active' : ''}`}
            href="#expenses"
            onClick={() => setActiveSection('expenses')}
          >
            Expenses
          </a>
          <a
            className={`nav-link ${activeSection === 'housemates' ? 'active' : ''}`}
            href="#housemates"
            onClick={() => setActiveSection('housemates')}
          >
            Housemates
          </a>
        </nav>

        <div className="profile-chip" aria-label="Signed in as Doga">
          <span className="profile-avatar">D</span>
          <span>Doga</span>
        </div>
      </header>

      <main className="page-content" id="home">
        <section className="welcome-section" id="dashboard">
          <div>
            <p className="eyebrow">YOUR HOME, IN HARMONY</p>
            <h1>Shared living,<br /><span>made simpler.</span></h1>
            <p className="intro-text">
              Keep track of household expenses, see who owes what, and make sharing a home feel effortless.
            </p>
          </div>
          <div className="welcome-illustration" aria-hidden="true">
            <div className="sun-shape" />
            <div className="house-shape">
              <div className="roof-shape" />
              <div className="house-body">
                <div className="house-window window-left" />
                <div className="house-door" />
                <div className="house-window window-right" />
              </div>
            </div>
            <div className="plant-shape plant-left"><span /><span /><span /></div>
            <div className="plant-shape plant-right"><span /><span /><span /></div>
          </div>
        </section>

        <div className="dashboard-actions">
          <button className="add-expense-button" type="button" onClick={() => setIsAddExpenseOpen(true)}>
            ＋ Add expense
          </button>
        </div>

        {isAddExpenseOpen && (
          <div
            className="modal-backdrop"
            onClick={(event) => {
              if (event.target === event.currentTarget) setIsAddExpenseOpen(false)
            }}
          >
            <section
              className="add-expense-modal"
              role="dialog"
              aria-modal="true"
              aria-labelledby="add-expense-title"
            >
              <div className="section-card-heading">
                <div>
                  <p className="eyebrow">HOUSEHOLD SPENDING</p>
                  <h2 id="add-expense-title">Add an expense</h2>
                </div>
                <button
                  className="modal-close-button"
                  type="button"
                  aria-label="Close add expense form"
                  onClick={() => setIsAddExpenseOpen(false)}
                >
                  ×
                </button>
              </div>
              <p className="intro-text">Record a shared cost for Flat 21E.</p>
              <form className="add-expense-form" onSubmit={handleAddExpense}>
                <label className="expense-form-field">
                  Description
                  <input
                    type="text"
                    value={newExpenseDescription}
                    onChange={(event) => setNewExpenseDescription(event.target.value)}
                    placeholder="e.g. Weekly groceries"
                    required
                    maxLength={120}
                  />
                </label>
                <div className="expense-form-grid">
                  <label className="expense-form-field">
                    Amount (£)
                    <input
                      type="number"
                      min="0.01"
                      step="0.01"
                      value={newExpenseAmount}
                      onChange={(event) => setNewExpenseAmount(event.target.value)}
                      placeholder="0.00"
                      required
                    />
                  </label>
                  <label className="expense-form-field">
                    Date
                    <input
                      type="date"
                      value={newExpenseDate}
                      onChange={(event) => setNewExpenseDate(event.target.value)}
                      required
                    />
                  </label>
                </div>
                <div className="expense-form-grid">
                  <label className="expense-form-field">
                    Paid by
                    <select value={newExpensePaidBy} onChange={(event) => setNewExpensePaidBy(event.target.value)}>
                      <option value="1">Doga</option>
                      <option value="2">April</option>
                      <option value="3">Ecren</option>
                    </select>
                  </label>
                  <label className="expense-form-field">
                    Category
                    <select value={newExpenseCategory} onChange={(event) => setNewExpenseCategory(event.target.value)}>
                      <option value="GROCERIES">Groceries</option>
                      <option value="UTILITIES">Utilities</option>
                      <option value="RENT">Rent</option>
                      <option value="HOUSEHOLD">Household</option>
                      <option value="TRANSPORT">Transport</option>
                      <option value="ENTERTAINMENT">Entertainment</option>
                      <option value="OTHER">Other</option>
                    </select>
                  </label>
                </div>
                <fieldset className="expense-split-fieldset">
                  <legend>Split equally between</legend>
                  {[
                    { id: '1', name: 'Doga' },
                    { id: '2', name: 'April' },
                    { id: '3', name: 'Ecren' },
                  ].map((housemate) => (
                    <label className="expense-split-option" key={housemate.id}>
                      <input
                        type="checkbox"
                        checked={Boolean(newExpenseSplits[housemate.id])}
                        onChange={(event) => setNewExpenseSplits((current) => ({
                          ...current,
                          [housemate.id]: event.target.checked,
                        }))}
                      />
                      <span>{housemate.name}</span>
                    </label>
                  ))}
                </fieldset>
                {expenseSubmitError && <p className="expense-form-error" role="alert">{expenseSubmitError}</p>}
                <div className="modal-actions">
                  <button className="modal-cancel-button" type="button" onClick={() => setIsAddExpenseOpen(false)} disabled={expenseSubmitting}>
                    Cancel
                  </button>
                  <button className="add-expense-button" type="submit" disabled={expenseSubmitting}>
                    {expenseSubmitting ? 'Saving…' : 'Save expense'}
                  </button>
                </div>
              </form>
            </section>
          </div>
        )}

        <section className="section-heading">
          <div>
            <p className="eyebrow">AT A GLANCE</p>
            <h2>Your household</h2>
          </div>
          <span className="household-badge"><span className="status-dot" /> Flat 21E</span>
        </section>

        <section className="summary-grid" aria-label="Household summary">
          <article className="summary-card balance-card">
            <div className="summary-card-top"><span className="card-symbol balance-symbol">↗</span><span className="card-label">YOUR BALANCE</span></div>
            <p className="summary-value">
              {balancesLoading
                ? 'Loading…'
                : balancesError
                  ? 'Unavailable'
                  : `${dogaBalance < 0 ? '-' : ''}£${Math.abs(dogaBalance).toFixed(2)}`}
            </p>
            <p className="summary-note">
              {balancesLoading
                ? 'Loading household balances'
                : balancesError
                  ? 'Could not load balances'
                  : dogaBalance > 0
                    ? "You're owed this amount"
                    : dogaBalance < 0
                      ? 'You owe this amount'
                      : 'You are all settled up'}
            </p>
          </article>
          <article className="summary-card spending-card">
            <div className="summary-card-top"><span className="card-symbol spending-symbol">▤</span><span className="card-label">HOUSEHOLD SPENDING</span></div>
            <p className="summary-value">
              {expensesLoading
                ? 'Loading…'
                : expensesError
                  ? 'Unavailable'
                  : `£${totalSpending.toFixed(2)}`}
            </p>
            <p className="summary-note">
              {expensesError ? 'Could not load expenses' : `${expenses.length} recorded expenses`}
            </p>
          </article>
          <article className="summary-card people-card">
            <div className="summary-card-top"><span className="card-symbol people-symbol">♧</span><span className="card-label">HOUSEMATES</span></div>
            <p className="summary-value">3 <span className="value-unit">people</span></p>
            <p className="summary-note">Making a home together</p>
          </article>
        </section>

        <section className="lower-grid">
          <article className="content-card expenses-card" id="expenses">
            <div className="section-card-heading">
              <div><p className="eyebrow">THE LATEST</p><h2>Recent expenses</h2></div>
              <a className="text-link" href="#expenses">View all <span>→</span></a>
            </div>
            {expensesLoading ? (
              <div className="empty-state">
                <div className="empty-icon">£</div>
                <h3>Loading expenses…</h3>
                <p>Your household costs are on their way.</p>
              </div>
            ) : expensesError ? (
              <div className="empty-state">
                <div className="empty-icon">!</div>
                <h3>Expenses could not load</h3>
                <p>Check that the FlatMate backend is running, then refresh the page.</p>
              </div>
            ) : expenses.length === 0 ? (
              <div className="empty-state">
                <div className="empty-icon">£</div>
                <h3>No expenses yet</h3>
                <p>Add your first shared cost to start tracking household spending.</p>
              </div>
            ) : (
              <div className="expense-list">
                {[...expenses].sort((a, b) => b.id - a.id).slice(0, 5).map((expense) => (
                  <div className="expense-row" key={expense.id}>
                    <span className="expense-icon">£</span>
                    <span className="expense-details">
                      <strong>{expense.description || expense.title || `Expense #${expense.id}`}</strong>
                      <small>{expense.category || 'Household expense'}</small>
                    </span>
                    <strong className="expense-amount">
                      £{Number(expense.amount).toFixed(2)}
                    </strong>
                  </div>
                ))}
              </div>
            )}
          </article>

          <aside className="content-card housemates-card" id="housemates">
            <div className="section-card-heading">
              <div><p className="eyebrow">YOUR CREW</p><h2>Housemates</h2></div>
              <span className="people-count">03</span>
            </div>
            <div className="housemate-row">
              <span className="person-avatar avatar-one">D</span>
              <span className="person-details"><strong>Doga</strong><small>You · Admin</small></span>
              <strong className={`housemate-balance ${!balancesLoading && !balancesError && Number(balances['Doga'] ?? 0) > 0 ? 'positive' : !balancesLoading && !balancesError && Number(balances['Doga'] ?? 0) < 0 ? 'negative' : ''}`}>
                {balancesLoading ? '…' : balancesError ? '—' : `${Number(balances['Doga'] ?? 0) < 0 ? '-' : ''}£${Math.abs(Number(balances['Doga'] ?? 0)).toFixed(2)}`}
              </strong>
            </div>
            <div className="housemate-row">
              <span className="person-avatar avatar-two">A</span>
              <span className="person-details"><strong>April</strong><small>Housemate</small></span>
              <strong className={`housemate-balance ${!balancesLoading && !balancesError && Number(balances['April'] ?? 0) > 0 ? 'positive' : !balancesLoading && !balancesError && Number(balances['April'] ?? 0) < 0 ? 'negative' : ''}`}>
                {balancesLoading ? '…' : balancesError ? '—' : `${Number(balances['April'] ?? 0) < 0 ? '-' : ''}£${Math.abs(Number(balances['April'] ?? 0)).toFixed(2)}`}
              </strong>
            </div>
            <div className="housemate-row">
              <span className="person-avatar avatar-three">E</span>
              <span className="person-details"><strong>Ecren</strong><small>Housemate</small></span>
              <strong className={`housemate-balance ${!balancesLoading && !balancesError && Number(balances['Ecren'] ?? 0) > 0 ? 'positive' : !balancesLoading && !balancesError && Number(balances['Ecren'] ?? 0) < 0 ? 'negative' : ''}`}>
                {balancesLoading ? '…' : balancesError ? '—' : `${Number(balances['Ecren'] ?? 0) < 0 ? '-' : ''}£${Math.abs(Number(balances['Ecren'] ?? 0)).toFixed(2)}`}
              </strong>
            </div>
            <button className="invite-button" type="button">＋ Invite a housemate</button>
          </aside>
        </section>

        <footer className="page-footer"><span>Made for happier shared homes.</span><span>FlatMate<span className="brand-period">.</span> <span className="footer-divider">·</span> Your household, in harmony.</span></footer>
      </main>
    </div>
  )
}

export default App