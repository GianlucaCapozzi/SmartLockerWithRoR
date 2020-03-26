Rails.application.routes.draw do
  # For details on the DSL available within this file, see https://guides.rubyonrails.org/routing.html
  
  post 'login', to: 'authentication#authenticate'
  post 'logout', to: 'blacklisting#blacklist'
  get 'activation/:id', to: 'registration#confirm_email'

  post 'signup', to: 'registration#registrate'
  post 'confprofile', to: 'configuration#configure'
  post 'forgetpass', to: 'recovery#forget'
  post 'recoverypass', to: 'recovery#change_pass_recovered'
  post 'changepass', to: 'recovery#change_pass'
  post 'changemail', to: 'recovery#change_email'
  
end
