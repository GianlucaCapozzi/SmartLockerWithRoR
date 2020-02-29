Rails.application.routes.draw do
  # For details on the DSL available within this file, see https://guides.rubyonrails.org/routing.html
  
  post 'login', to: 'authentication#authenticate'
  post 'logout', to: 'blacklisting#blacklist'
  post 'activation/:id', to: 'registration#confirm_email'

  post 'signup', to: 'registration#registrate'
  post 'confsignup', to: 'configuration#configure'
  post 'forgetpass', to: 'recovery#forget'
  post 'recoverypass', to: 'recovery#change_pass'
  
end
