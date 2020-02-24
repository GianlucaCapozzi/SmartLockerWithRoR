class RegisterUser
    prepend SimpleCommand

    def initialize(username, email, password)
        @username = username
        @email = email
        @password = password
    end

    def call
        User.create(username: @username, email: @email , password: @password , password_confirmation: @password) if signup_check
    end

    private

    attr_accessor :username, :email, :password

    def signup_check
        exist_username = User.exists?(username: @username)
        exist_email = User.exists?(email: @email)

        if exist_username and exist_email
            errors.add :user_registration, 'username and email'
            return false
        elsif exist_username
            errors.add :user_registration, 'username'
            return false
        elsif exist_email
            errors.add :user_registration, 'email'
            return false
        else
            return true
        end
        
    end
end