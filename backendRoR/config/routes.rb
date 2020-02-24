Rails.application.routes.draw do
  # For details on the DSL available within this file, see https://guides.rubyonrails.org/routing.html
  
  get 'login', to: 'authentication#authenticate'
  #post 'logout', to: 'authentication#blacklist'

  post 'signup', to: 'registration#registrate'
  
end
