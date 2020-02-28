Rails.application.routes.draw do
  # For details on the DSL available within this file, see https://guides.rubyonrails.org/routing.html
  
  get 'login', to: 'authentication#authenticate'
  get 'logout', to: 'blacklisting#blacklist'
  get 'activation/:id', to: 'registration#confirm_email'

  post 'signup', to: 'registration#registrate'
  post 'confsignup', to: 'configuration#configure'
  post 'forgetpass', to: 'recovery#forget'
  post 'recoverypass', to: 'recovery#change_pass'
  
end
